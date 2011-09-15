lyricFetcher version 1.0

===ABOUT===
This program constructs queries to LyricWiki <http://lyrics.wikia.com/Main_Page>, each composed of an artist and song name. The target page is parsed and the lyrics (if any) are saved as a plaintext file. The lyrics are rudimentarily cleaned by removing extra spaces, deleting lines that begin with stopwords (such as 'verse' and 'bridge') and copying the text of choruses whenever the word 'chorus' is used as a placeholder.

===HOW TO RUN===
Before running, make sure Ruby is installed:
<http://www.ruby-lang.org/>

Also make sure that the 'net/http', 'uri' and 'iconv' packages are installed (these are part of a standard Ruby installation).

The script requires a set of three files, each containing the artists, song titles, and output filenames of the queries. Therefore, to run the script you should prepare three plaintext files as input: artists.txt, titles.txt, and hashes.txt (these can easily be renamed within the script), where the Nth row of each file should contain the artist, song title, and output filename of the Nth query.

For instance, they could begin like so:

artists.txt:          titles.txt:            hashes.txt:
"Lady GaGa            "Paparazzi             "song_id_1
 Cher                  Believe                song_id_2
 Grizzly Bear          Veckatimest            song_id_3
 ..."                  ..."                   ..."

When ready to run, simply type "ruby lyricFetcher.rb" at the prompt. The lyrics are saved to the ./lyrics directory (which is automatically created).

===BUGS===
Known bug: Script occasionally (~ 1 per 2000 queries) throws a segmentation fault. This appears to be a bug in the Net package, and not directly caused by the script, but it is not known how to handle this error. This can be a nuisance when collecting >>1000 sets of lyrics, so the script is designed to output statistics and record the index of the last successful query. The script's main loop (line 248) can then be edited to restart at the appropriate query.