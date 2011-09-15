#!/usr/bin/env python

# lyric_features.py [DIRECTORY_NAME] prints an ACE XML 1.1 feature
# value file to standard output with the features corresponding to the
# lyrics in the specified directory

import csv
import os
import os.path
import string
import subprocess
import sys

function_words = [["the"],
                  ["and"],
                  ["to"],
                  ["a", "an"],
                  ["of"],
                  ["in"],
                  ["that", "those"],
                  ["it"],
                  ["not"],
                  ["as"],
                  ["with"],
                  ["but"],
                  ["for"],
                  ["at"],
                  ["this", "these"],
                  ["so"],
                  ["all"],
                  ["on"],
                  ["from"],
                  ["one", "ones"],
                  ["up"],
                  ["no"],
                  ["out"],
                  ["what"],
                  ["then"],
                  ["if"],
                  ["there"],
                  ["by"],
                  ["who"],
                  ["when"],
                  ["into"],
                  ["now"],
                  ["down"],
                  ["over"],
                  ["back"],
                  ["or"],
                  ["well"],
                  ["which"],
                  ["how"],
                  ["here"],
                  ["just"],
                  ["very"],
                  ["where"],
                  ["before"],
                  ["upon"],
                  ["about"],
                  ["after"],
                  ["more"],
                  ["why"],
                  ["some"]]

treebank_tags = [['CC'],
                 ['CD'],
                 ['DT'],
                 ['EX'],
                 ['FW'],
                 ['IN'],
                 ['JJ', 'JJR', 'JJS'],
                 ['LS'],
                 ['MD'],
                 ['NN', 'NNS', 'NNP', 'NNPS'],
                 ['PDT'],
                 ['POS'],
                 ['PRP', 'PRP$'],
                 ['RB', 'RBR', 'RBS'],
                 ['RP'],
                 ['SYM'],
                 ['TO'],
                 ['UH'],
                 ['VB', 'VBD', 'VBG', 'VBN', 'VBP', 'VBZ'],
                 ['WDT', 'WP' 'WP$', 'WRB']]

def print_feature(name, value_list):
    print("      <feature>\n"
          + "         <name>"
          + name
          + "</name>")
    for v in value_list:
        print("         <v>"
              + str(v)
              +"</v>")
    print("      </feature>")

def function_word_frequencies(lyrics):
    length = len(lyrics)
    if length == 0: return [0 for x in range(50)]
    return [float(sum([sum([w == y for y in lyrics]) for w in l])) 
            / length
            for l in function_words]

def average_word_length(lyrics):
    length = len(lyrics)
    if length == 0: return [0]
    return [float(sum([len(s) for s in lyrics])) / length]

def letter_frequencies(lyrics):
    length = len(lyrics)
    if length == 0: return [0 for x in range(26)]
    join = "".join(lyrics)
    join_length = len(join)
    return [float(join.count(chr(i))) / join_length for i in range(97, 123)]

def punctuation_frequencies(lyrics):
    length = len(lyrics)
    if length == 0: return [0 for x in range(32)]
    return [float(sum([l.count(p) for l in lyrics])) / length
            for p in string.punctuation]

def vocabulary_richness(lyrics):
    length = len(lyrics)
    if length == 0: return [0]
    return [float(len(frozenset(lyrics))) / length]

def flesh_list(filename):
    process = subprocess.Popen(['java', '-jar', 'CmdFlesh.jar', filename],
                               stdout = subprocess.PIPE)
    process.wait()
    result = process.stdout.read()
    process.stdout.close()
    split = result.replace(',', '.').replace('?', '0').split()
    return [split[i] for i in [3, 8, 10, 12, 17, 22]]

def aspell_count(lyrics):
    length = len(lyrics)
    if length == 0: return [0]
    process = subprocess.Popen(['aspell', '-d', 'en', 'list'],
                               stdin = subprocess.PIPE,
                               stdout = subprocess.PIPE)
    (result, stderrdata) = process.communicate(" ".join(lyrics))
    return [float(len(result.split())) / length]

def treebank_frequencies(filename):
    process = subprocess.Popen(['./stanford-postagger.sh',
                                'models/bidirectional-distsim-wsj-0-18.tagger',
                                filename],
                               cwd = 'stanford-postagger-2009-12-24',
                               stdout = subprocess.PIPE)
    process.wait()
    result = process.stdout.read()
    process.stdout.close()
    split = result.split()
    length = len(split)
    if length == 0: return [0 for x in range(20)]
    triples = [s.partition('_') for s in result.split()]
    return [float(sum([sum([t == y[2] for y in triples]) for t in l])) 
            / length
            for l in treebank_tags]

def bigram_frequencies(lyrics):
    characters = " " + string.ascii_lowercase
    length = len(lyrics)
    if length == 0: return ",".join(["0" for x in range(27**2)]) + "\n"
    join = " ".join(lyrics)
    join_length = len(join)
    bigrams = [s1 + s2 for s1 in characters for s2 in characters]
    return ",".join(["%g" % (float(join.count(b)) / join_length) for b in bigrams]) + "\n"

features = [("Word count", lambda f, l, r: [len(l)]),
            ("Function word frequencies", 
             lambda f, l, r: function_word_frequencies(r)),
            ("Average word length", lambda f, l, r: average_word_length(r)),
            ("Letter frequencies", lambda f, l, r: letter_frequencies(r)),
            ("Punctuation frequencies",
             lambda f, l, r: punctuation_frequencies(l)),
            ("Vocabulary size", 
             lambda f, l, r: [len(frozenset([s.lower() for s in r]))]),
            ("Vocabulary richness", lambda f, l, r: vocabulary_richness(r)),
            ("Flesh-Kincaid grade level", lambda f, l, r: flesh_list(f)[0:1]),
            ("Flesh reading ease", lambda f, l, r: flesh_list(f)[1:2]),
            ("Sentence count", lambda f, l, r: flesh_list(f)[2:3]),
            ("Average syllable count per word",
             lambda f, l, r: flesh_list(f)[4:5]),
            ("Average sentence length", lambda f, l, r: flesh_list(f)[5:6]),
            ("Rate of misspelling", lambda f, l, r: aspell_count(l)),
            ("Part-of-speech frequencies",
             lambda f, l, r: treebank_frequencies(f))]

intermediate_features = [("bigram_frequencies.dat",
                          lambda c, l, r: bigram_frequencies(r))]

csv_features = [("Letter-bigram components",
                 "bigram_components.dat"),
                ("Topic membership probabilities (10 topics)",
                 "topics10.dat"),
                ("Topic membership probabilities (24 topics)",
                 "topics24.dat")]

print('<?xml version="1.0"?>\n'
      + "   <!DOCTYPE feature_vector_file [\n"
      + "   <!ELEMENT feature_vector_file (comments, data_set+)>\n"
      + "   <!ELEMENT comments (#PCDATA)>\n"
      + "   <!ELEMENT data_set (data_set_id, section*, feature*)>\n"
      + "   <!ELEMENT data_set_id (#PCDATA)>\n"
      + "   <!ELEMENT section (feature+)>\n"
      + '   <!ATTLIST section start CDATA ""\n'
      + '                     stop CDATA "">\n'
      + "   <!ELEMENT feature (name, v+)>\n"
      + "   <!ELEMENT name (#PCDATA)>\n"
      + "   <!ELEMENT v (#PCDATA)>\n"
      + "]>\n\n"
      + "<feature_vector_file>\n\n"
      + "   <comments>Features extracted for SLAC</comments>\n")

csv_files = [(n, open(os.path.join(f))) for (n, f) in csv_features]
csv_readers = [(n, csv.reader(f)) for (n, f) in csv_files]
for filename in os.listdir(sys.argv[1]):
    if filename == ".DS_Store": continue
    print("   <data_set>\n"
          + "      <data_set_id>"
          + os.path.splitext(filename)[0]
          + "</data_set_id>")
    full_name = os.path.join(sys.argv[1], filename)
    file = open(full_name)
    contents = file.read()
    file.close()
    lyrics = contents.split()
    reduced_lyrics = [s.translate(None, string.punctuation).lower() for s in lyrics]
    for feature in features:
        print_feature(feature[0], feature[1](full_name, lyrics, reduced_lyrics))
    for feature in intermediate_features:
        outside_file = open(feature[0], 'a')
        outside_file.write(feature[1](contents, lyrics, reduced_lyrics))
        outside_file.close()
    for feature in csv_readers:
        print_feature(feature[0], feature[1].next())
    print("   </data_set>")
for (n, f) in csv_files: f.close()

print("</feature_vector_file>")






