require 'net/http'
require 'uri'
require 'iconv'

# Subroutine cleans artist and song fields, queries lyricwiki, and extracts body of web page as string.
def getLyricsPage
  # Format artist and song name for LyricWiki query by replacing spaces with underscores:
  $artist.gsub!(' ','_')
  $song.gsub!(' ','_')
  
  if $most_recent_query != [$artist, $song] then
    # Construct URL to query:
    url = 'http://lyrics.wikia.com/'
    url += "index.php?title=#{$artist}:#{$song}&action=edit"
    puts "      Calling: <" + url + ">\n"
    # Fetch and extract website: (old way, using URI)
    begin
      lyrics = nil
      res = Net::HTTP.get_response(URI.parse(url))
      begin
        lyrics = res.body
        res = []
      ensure
        lyrics = "" unless !lyrics.nil?
        puts "      NB: Slow response; request failed." unless !lyrics.nil?
        $statistics[3] += "\n#{$filename}: #{$song} - #{$artist}" unless !lyrics.nil?
      end
    rescue URI::InvalidURIError
      lyrics = ""
      puts "      NB: Invalid request; request failed."
      res = []
      $statistics[2] += "\n#{$filename}: #{$song} - #{$artist}"
    end
    
  else
    # We're caught in a redirect loop! This happens when the capitalization format is not being obeyed, for instance.
    lyrics = ""
    puts "      NB: We got caught in a redirect loop."
    $statistics[6] += "\n#{$filename}: #{$song} - #{$artist}"
  end

  $most_recent_query = [$artist, $song]
  
  # Get rid of everything but the lyrics, based on the two lyric tag identifiers.
  # Note: the first tag identifier is not unique, since a written instruction mentions the tag. But the tag appears to uniquely occur directly after a newline \n.
  # Note: some pages say <lyrics>, some <lyric>
  lyrics.gsub!(/.*\n&lt;lyrics?&gt;/m,'')
  lyrics.gsub!(/&lt;\/lyrics?&gt;.*/m,'')
  
  if lyrics.split(/#REDIRECT /i)[1] != nil then
    lyrics = getRedirect(lyrics)
  end
  return lyrics
end

def getRedirect(lyrics)
  # Chop the redirect up into its constituant parts:
    newQuery = lyrics.split(/#REDIRECT /i)[1]
    fields = newQuery.scan(/\[\[.*\]\]/).to_s
    fields = fields.split("[[")[1].split("]]")[0].split(":")
    $statistics[1] += "\n#{$filename}: #{$song} - #{$artist} ===> "
  # Store the new artist and song song:
    $artist = fields[0]
    $song = fields[1]
        # Note: sometimes there's a pipe in the second part:
        # Alice_In_Chains:Would?|Would? was discovered in the wild. This line fixes that.
        $song = $song.split("|")[0]
    puts "      NB: Redirected."
    lyrics = getLyricsPage
    $statistics[1] += "#{$filename}: #{$song} - #{$artist}"
    return lyrics
end


def copyChoruses(lyrics, stopword)
    # Sometimes, a stanza will be labeled as "chorus." Then wherever "chorus" reoccurs, it's meant to be replaced by the original chorus (a shortcut).
    # Other times, all the choruses are written out, but each is labeled as "chorus" anyway (a label).
    # Our heuristic for distinguishing between these two cases (shortcut or label) is this:
    # Supposing the first instance of the word "chorus" is followed by line A, and the second instance of "chorus" is followed by line B, then we assume the word "chorus" is acting as a shortcut if A!=B, and we assume it's just a label if A=B.
  
    # 1. Split the song into stanzas (wherever there's a double break) indexed by 0..(# of stanzas-1).
    stanzas = lyrics.split(/\n{2,}/)
    chorusMatches = []
    
    # 2. Find stanzas (by index) that begin with a stopword:
    (0..stanzas.length-1).each do |index|
      # If the stanza begins with the stopword...
      if !stanzas[index].scan(/\A[^a-z0-9\n]*#{stopword}.*/i).empty? then
        # ...remember that stanza.
        chorusMatches << index
      end
    end
    
    if !chorusMatches.empty?
      # 3. Now we can grab the text of the chorus.
      # Sometimes the first chorus label is immediately prior to the chorus text...
      if stanzas[chorusMatches[0]].split("\n").length > 1 then
        chorusText = stanzas[chorusMatches[0]]
      # ...but sometimes the chorus text lies in the next stanza:
      else        
        chorusText = stanzas[chorusMatches[0]+1]
      end
      
      # 4. Replace each of the other stopwords with the chorustext.
      (1..chorusMatches.length-1).each do |index|
        stanzas[chorusMatches[index]] = chorusText
      end
      # 5. Let the stats file know!
      $statistics[4] += "\n#{$filename}: #{$song} - #{$artist}. Copied choruses labelled with \'#{stopword}.\'"
    end

    # 6. Stitch up the set of (potentially new) stanzas:
    lyrics = stanzas.join("\n\n")
    return lyrics
end


def getPrettyLyrics(dirtyLyrics)
  begin
    cleanLyrics = "#{Iconv.iconv('ascii//translit','UTF-8',dirtyLyrics)}"
  rescue Iconv::IllegalSequence
    cleanLyrics = ""
    puts "      NB: Something garbled the lyrics conversion."
    $statistics[5] += "\n#{$filename}: #{$song} - #{$artist}"
  end
  
  # Collect extra newlines and needless spaces at the beginnings and ends of lines:
  cleanLyrics.gsub!(/\n{3,}/,"\n\n")
  cleanLyrics.gsub!(/ ?\n/,"\n")
  cleanLyrics.gsub!(/\n ?/,"\n")
  cleanLyrics.gsub!(/ {2,}/," ")
  # Some characters are left encoded. Let's replace them automagically:
  cleanLyrics.gsub!("&amp;","&")
  cleanLyrics.gsub!("&quot;","\"")
  
  # We have to get rid of lines that begin with stopwords. We don't
  # care if there are any non-alphanumeric characters before the stopword.
  cleanLyrics.gsub!(/\n[^a-z0-9\n]*(verse|bridge|intro|repeat|outro|instrumental).*?\n/i,"\n\n")
  
  chorusWords = ["chorus", "refrain", "hook"]

  # Chorus copying:
  chorusWords.each do |stopword|
    if !cleanLyrics.scan(/\n[^a-z0-9\n]*#{stopword}.*?\n/i).empty? then
      # The following REGEX matches lines that contain the word 'chorus' (after any number of non-alphanumeric characters, to allow for "==chorus==" and the like). So the song will be split at each 'chorus' line. Then we just need to compare the first lines of the result:
      tmp = cleanLyrics.split(/(\n|\A)[^a-z0-9\n]*#{stopword}.*\n*/i)
      if tmp.length > 2 then
        if tmp[1].scan(/.*\n/)[0] != tmp[2].scan(/.*\n/)[0] then
          cleanLyrics = copyChoruses(cleanLyrics, stopword)
        end
      end
    end
  end
    
  # The first occurances of the stopwords, if they let to chorus-copying, are still around. Get rid of them!
  cleanLyrics.gsub!(/\n[^a-z0-9\n]*(chorus|refrain|hook).*?\n/i,"\n\n")
  
  # Double check that there are no collections of newlines longer than 2:
  cleanLyrics.gsub!(/(\n){3,}/,"\n\n")
  
  # Also clean extra newlines from the beginning and end of the lyrics.
  cleanLyrics.gsub!(/\A(\n)*/,"")
  cleanLyrics.gsub!(/(\n)*\Z/,"")
    
  # Check that the page we arrived at isn't a placeholder page for a non-existent song.
  if cleanLyrics.scan("PUT LYRICS HERE (and delete this") != [] then
    puts "      NB: No lyrics.\n"
    cleanLyrics = ""
    $statistics[0] += "\n#{$filename}: #{$song} - #{$artist}"
  end
  return cleanLyrics
end


def capitalizeWords(phrase)
    phraseWords = phrase.split(' ')
    phraseWords.each do |word|
      if word.scan(/\A[a-z]/) != [] then
        word.capitalize!
      end
    end
    titleCase = phraseWords.join(' ')
    return titleCase
end


def writeFile(lyrics)
  outFile = File.new("lyrics/#{$filename}.txt", "w")
  outFile.write(lyrics)
  outFile.close
end


def cleanKeysFile(filename)
  namesFile = File.open("#{filename}", "r")
  names = namesFile.readlines
  editedNames = []
  names.each_with_index do |name, index|
    editedNames[index] = name.gsub(/[^a-zA-Z0-9_\n]/, "_")
  end
  newKeysFile = "Edited_" + filename
  namesFile.close
  newNamesFile = File.open("#{newKeysFile}", 'w')
  newNamesFile.write(editedNames)
  newNamesFile.close
  return newKeysFile
end


begin
  #### Read in files containing queries:
  newKeysFile = cleanKeysFile("hashes.txt")
    # Read in artist and song names...
  titlesFile = File.open("titles.txt","r")
  artistsFile = File.open("artists.txt","r")
  keysFile = File.open(newKeysFile,"r")
  system("mkdir lyrics")
    # ...save the contents of the files to variables...
  titles = titlesFile.readlines
  artists = artistsFile.readlines
  keys = keysFile.readlines
    # ...and close the files.
  titlesFile.close()
  artistsFile.close()
  keysFile.close()


  # Set up a variable that will contain statistics and save it to file.
  songsWithoutLyrics = "\n\nThe following hashes indicate songs that have empty lyrics:"
  songsWithRedirects = "\n\nThe following queries to LyricWiki resulted in redirects:"
  failedRequests = "\n\nThe following songs led to queries that failed:"
  slowRequests = "\n\nThe following songs led to queries that timed out:"
  choruswordsCopied = "\n\nIn the following songs, choruses were detected and copied accordingly."
  conversionErrors = "\n\nWhen parsing the following songs, some error occurred due to ICONV."
  loopySongs = "\n\nThe redirects for these songs sent our program into an endless loop."
  $statistics = [songsWithoutLyrics, songsWithRedirects, failedRequests, slowRequests, choruswordsCopied, conversionErrors, loopySongs]

  upto_index = 0

  # For each artist,
  (0..artists.length-1).each do |index|
    # Read in artist and song names:
    $artist = artists[index].chomp
    $song = titles[index].chomp
    $filename = keys[index].chomp
    puts "Processing song \##{index+1}: #{$song} - #{$artist}..."
    
    # Get and clean lyrics:
    $artist = capitalizeWords($artist)
    $song = capitalizeWords($song)
    lyrics = getLyricsPage
    if lyrics != "" then
      lyrics = getPrettyLyrics(lyrics)
    end
    
    # Write lyrics to file:
    writeFile(lyrics)
    upto_index = index
  end

ensure
  # If the script errs fatally, we still want to output the statistics we've compiled so far:
  statsFile = File.open("lyrics/statistics_upto_#{upto_index}.txt", "w")
  statsFile.write($statistics)
  statsFile.close
end