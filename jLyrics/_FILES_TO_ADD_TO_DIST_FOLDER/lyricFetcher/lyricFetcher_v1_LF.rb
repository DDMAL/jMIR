require 'net/http'
require 'uri'
require 'iconv'
require 'lyricsfly_key'
require 'rexml/document'

# query LyricsFly - retrieve lyrics
def getLyricsPage
  # Format artist and song name for LyricsFly query:
    # Replace space with + and swap alphanumerics for %:
  lyrics = []
  $artist.gsub!(' ','+')
    $song.gsub!(' ','+')
  queryValidator($artist)
  queryValidator($song)

  # Construct URL to query:
  url = "http://api.lyricsfly.com/api/"
  url += "api.php?i=2026462a4692c21d7-temporary.API.access&a=#{$artist}&t=#{$song}"
  puts "      Calling: <" + url + ">\n"

  # Try querying:
  begin
    result = Net::HTTP.get_response(URI.parse(url))
    tag = "success"
  rescue
    # Sometimes you get a timeout error:
    lyrics = []
    puts "      NB: Timeout error!\n"
    $statistics[1] += "\n#{$filename}: #{$song} - #{$artist}"
    lyricsrequest = "fail"
  end
  # If we didn't get a timeout error, then collect the XML into our lyrics file:
  if tag == "success" then
    xml = REXML::Document.new(result.body)
    lyrics = [];
    xml.elements.each("start/sg") do |e|
      lyrics = lyrics << e.elements['tx'].text unless e.elements['tx'].nil?
    end
    tag = "ditch"
  end
  return lyrics
end


def queryValidator(string)
  # LyricsFly prefers all non-alphanumeric characters to be replaced by "%":
  string.gsub!(/[^a-zA-Z0-9\+ ]/, "%25")
  # string.gsub!(/[^a-zA-Z0-9_\$\#\^\&\*\=\-\;\:\,\@\|\\\[\]\.\+\?\/\!]/, "%")
  return string
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
      $statistics[2] += "\n#{$filename}: #{$song} - #{$artist}. Copied choruses labelled with \'#{stopword}.\'"
    end

    # 6. Stitch up the set of (potentially new) stanzas:
    lyrics = stanzas.join("\n\n")
    return lyrics
end



def getPrettyLyrics(dirtyLyrics)
  
  # This subroutine eliminates extra spaces and non-lyics information, attempting to format the lyrics in a standard way. It also eliminates lines that begin with stopwords (verse, intro, etc.) and copies choruses.
  
  begin
    cleanLyrics = "#{Iconv.iconv('ascii//translit','UTF-8',dirtyLyrics)}"
  rescue Iconv::IllegalSequence
    cleanLyrics = ""
    puts "      NB: Iconv conversion error. Lyrics exist but not retrieved.\n"
    $statistics[3] += "\n#{$filename}: #{$song} - #{$artist}"
  end
  
  if cleanLyrics.scan(/[a-z]/) == [] then
    puts "      NB: Lyrics are empty.\n"
    $statistics[0] += "\n#{$filename}: #{$song} - #{$artist}"
    cleanLyrics = ""
  end

  # Sometimes we get multiple sets of lyrics. After the first set, the string "&#169;" appears to indicate a copyright notice or some other gobbledygook. So we snip the lyrics at every occurance of "&#169;" and take the first one. We also cut out the lyricsfly.com attribution and any multiple spaces.
  cleanLyrics = cleanLyrics.split("&#169;")[0]
  if cleanLyrics.nil? then cleanLyrics = "" end
  cleanLyrics.gsub!("Lyrics delivered by lyricsfly.com","")
  cleanLyrics.gsub!(/ {2,}/," ")
  
  # Sometimes there are huge strings of blank spaces. Let's reduce these:
  cleanLyrics.gsub!(/ {2,}/," ")
  # Sometimes lyrics appear to be redundantly labled with "[br]\n" in place of each anticipated "\n", so we eliminate these:
  cleanLyrics.gsub!("[br]\n","\n")
  cleanLyrics.gsub!("\n[br]","\n")
  # Sometimes lyrics are not labelled redundantly, and only the [br]s indicate breaks, so we replace these last:
  cleanLyrics.gsub!("[br]","\n")
  # Collect extra newlines and needless spaces at the beginnings and ends of lines:
  cleanLyrics.gsub!(/\n{3,}/,"\n\n")
  cleanLyrics.gsub!(/ ?\n/,"\n")
  cleanLyrics.gsub!(/\n ?/,"\n")
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
      tmp = cleanLyrics.split(/\n[^a-z0-9\n]*#{stopword}.*\n*/i)
      if tmp[1].scan(/.*\n/)[0] != tmp[2].scan(/.*\n/)[0] && tmp.length > 2 then
        cleanLyrics = copyChoruses(cleanLyrics, stopword)
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
  
  return cleanLyrics
  
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
    
  system("mkdir lyrics")
  #### Read in files containing queries:
  newKeysFile = cleanKeysFile("hashes.txt")
    # Read in artist and song names...
  titlesFile = File.open("titles.txt","r")
  artistsFile = File.open("artists.txt","r")
  keysFile = File.open(newKeysFile,"r")
    # ...save the contents of the files to variables...
  titles = titlesFile.readlines
  artists = artistsFile.readlines
  keys = keysFile.readlines
    # ...and close the files.
  titlesFile.close()
  artistsFile.close()
  keysFile.close()

  # Set up a variable that will contain error stats and save it to file:
  songsWithoutLyrics = "\n\nThe following hashes indicate songs that have empty lyrics:"
  failedRequests = "\n\nThe following songs led to queries that failed:"
  slowRequests = "\n\nThe following songs led to queries that timed out:"
  choruswordsCopied = "\n\nIn the following songs, choruses were detected and copied accordingly:"
  iconverrors = "\n\nIconv erros in the following songs:"
  $statistics = [songsWithoutLyrics, slowRequests, choruswordsCopied, iconverrors]

  upto_index = 0

  # For each artist,
  (0..artists.length-1).each do |index|
    # Read in artist and song names:
    $artist = artists[index].chomp
    $song = titles[index].chomp
    $filename = keys[index].chomp
    puts "Processing song \##{index+1}: #{$song} - #{$artist}..."
    
    # Get and clean lyrics:
    lyrics = getLyricsPage
    if lyrics != "" then
      lyrics = getPrettyLyrics(lyrics[0])
    end
    
    # Write lyrics to file:
    writeFile(lyrics)
    upto_index = index
    
    # Sleep 10 seconds to satisfy LyricsFly API:
    sleep(10.02)
  end
  
ensure
  # If the script errs fatally, we still want to output the statistics we've compiled so far:
  statsFile = File.open("lyrics/statistics_upto_#{upto_index}.txt", "w")
  statsFile.write($statistics)
  statsFile.close
end