# Author: Joao Goncalves
# Logger parser
__author__ = 'Joao Goncalves'

import re
import os
import sys
import math

class Measurement:
    pass


LOG_NAME = "log"
# Where the log files can be found
DIR_PRE = "/home/user/log_files/"
# Subdirectories, in case you produced several measurements
DIR = ["m1", "m2", "m3"]
# Another branch of subdirectories inside which separate different tests (You can leave this list empty)
VPS_COMPS = ["LOGS"]
# Keys to find within log files, without the _start and _stop suffixes
KEYS = ["some_stuff"]

queue = dict()
final_results = dict()
parse_errors = 0
parse_warnings = 0


def process_entry(entry):
    #print ">", entry  # DEBUG
    global parse_errors, parse_warnings

    try:
        if "step" in entry[2]:
            count = 0
            pos = 0
            for c in reversed(entry[2]):
                pos += 1
                if c == '_':
                    count += 1
                if count == 2:
                    under_index = len(entry[2]) - pos
                    break
        else:
            under_index = entry[2].rfind('_')

        pair = [entry[2][:under_index], entry[2][under_index+1:]]
    except IndexError, e:
        parse_errors += 1
        return

    key = pair[0]+entry[1]
    if key not in queue:
        if pair[1] == "start":
            m = Measurement()
            m.time = entry[0].split()[2]
            m.src = entry[1]
            m.id = entry[3]
            #print key, ">>", m.id, m.src, m.time
            queue[key] = m
        else:
            #print "Measurement -", pair[0], "- came without a start. Discarding..."
            parse_errors += 1
    else:
        m = queue[key]
        prev_time = m.time
        end_time = entry[0].split()[2]
        if pair[1] == "start":
            #print "New measurement -", pair[0], "- without prior termination. Assuming termination now..."
            parse_warnings += 1
            m = Measurement()
            m.time = entry[0].split()[2]
            m.src = entry[1]
            m.id = entry[3]
            queue[pair[0]] = m
        elif "step" in pair[1]:
            pass
        else:
            del queue[key]
            
        # The following section was unidented

        total_time = long(end_time) - long(prev_time)
        if pair[1] == "step":
            final_key = pair[0]+"_"+pair[1]+"_"+pair[2]
            #print "It took -", pair[0], "- on step", pair[2], "exactly", total_time, "ms to execute"
        else:
            final_key = pair[0]
            #print "It took -", pair[0], "- exactly", total_time, "ms to execute"

        if final_key not in final_results:
            final_results[final_key] = []

        final_results[final_key].append(total_time)


# Initialize main variables
final_sampled_results = dict()

run_dir_pattern = re.compile('(run).*')
external_fname_pattern = re.compile('.*(_cf_external_log).*')
log_pattern = re.compile('\[(.*?)\]')

try:
    os.mkdir('results')
except OSError:
    pass

# List all run directories

try:
    run_list = os.listdir(DIR_PRE)
except OSError, e:
    print "Invalid directory:", DIR_PRE
    sys.exit()

run_list = filter(lambda i: run_dir_pattern.search(i), run_list)
if len(run_list) == 0:
    run_list = ["."]

# Travel across all directories

runs_compendium = dict()

for run in run_list:

    runs_compendium[run] = dict()

    for cur_component in VPS_COMPS:

        for cur_dir in DIR:
            cur_dir = run+"/"+cur_component+"/"+cur_dir
            print "## Entering directory", DIR_PRE+cur_dir

            try:
                file_list = os.listdir(DIR_PRE+cur_dir)
            except OSError, e:
                print "Invalid directory:", DIR_PRE+cur_dir
                continue

            # Strip unnecessary files
            file_list = filter(lambda i: external_fname_pattern.search(i), file_list)

            # Parsing regular measurement files

            if len(file_list) < 1:
                print "No files found in:", DIR_PRE+cur_dir
                continue

            print "Files:", file_list
            for item in file_list:

                queue.clear()
                final_results.clear()
                final_sampled_results.clear()

                print "## Parsing", item
                with open(DIR_PRE+cur_dir+"/"+item, 'r') as tmp:
                    for line in tmp:
                        parts = log_pattern.findall(line)
                        process_entry(parts)

                for cur_key in KEYS:

                    final_sampled_results.clear()

                    if len(final_results) == 0:
                        continue

                    max_measurement_key = 0
                    try:
                        if len(final_results[cur_key]) > max_measurement_key:
                            max_measurement_key = len(final_results[cur_key])
                    except KeyError, e:
                        continue

                    split_factor = int(math.floor(max_measurement_key / 100))
                    if split_factor > 1:
                        final_sampled_results[cur_key] = [sum(final_results[cur_key][i:i+split_factor])/split_factor for i in range(0, len(final_results[cur_key]), split_factor)]
                    else:
                        final_sampled_results[cur_key] = list(final_results[cur_key])

                    max_measurement_key = 0
                    for v in final_sampled_results.itervalues():
                        if len(v) > max_measurement_key:
                            max_measurement_key = len(v)

                    runs_compendium[run][cur_dir[len(run)+1:]+"_"+LOG_NAME+"_"+cur_key] = final_sampled_results.copy()

# Create the graphs

runs_average = dict()
for item in runs_compendium.keys():
    for run_key in runs_compendium[item]:
        if run_key in runs_average:
            pos = 0
            for a in range(len(runs_compendium[item][run_key])):
                try:
                    if pos != 0:
                        runs_average[run_key][pos] += runs_compendium[item][run_key][pos]
                    pos += 1
                except IndexError, e:
                    break
        else:
            runs_average[run_key] = runs_compendium[item][run_key]

for item in runs_average.keys():
    if not runs_average[item]:
        continue

    for a in range(len(runs_average[item][runs_average[item].keys()[0]])):
        runs_average[item][runs_average[item].keys()[0]][a] /= len(runs_compendium.keys())

    print "Writing results..."
    file_counter = 0
    while os.path.exists("results/"+item+"_"+str(file_counter)):
        file_counter += 1

    if not os.path.exists(os.path.dirname("results/"+item+"_"+str(file_counter))):
        os.makedirs(os.path.dirname("results/"+item+"_"+str(file_counter)))

    transpose_results = runs_average[item]
    with open("results/"+item+"_"+str(file_counter), "w+") as f:
        f.write(transpose_results.keys()[0]+"\r\n")
        for a in transpose_results[transpose_results.keys()[0]]:
            f.write(str(a)+"\r\n")

    try:
        os.remove('.gnutmp')
    except OSError:
        pass

    with open(".gnutmp", "w") as f:
        f.write("set term pngcairo\n")
        f.write("set datafile separator ','\n")
        f.write("set key autotitle columnhead\n")
        f.write("set output 'results/"+item+"_"+str(file_counter)+".png'\n")
        tmp_str = "plot 'results/"+item+"_"+str(file_counter)+"' u 1 w lines"
        for a in range(len(final_sampled_results)-1):
            tmp_str += ", '' u "+str(a+2)+" w lines"
        f.write(tmp_str)

    os.system("gnuplot .gnutmp")

    print "Parsing", item, "complete"
    print "Errors:", parse_errors, "- Warnings:", parse_warnings

    parse_errors = 0
    parse_warnings = 0