# Author: Joao Goncalves
# Logger parser
__author__ = 'Joao Goncalves'

import os
import re
import math
from numpy import array

LOG_CPU_NAME = "velox_cpu_log"
LOG_MEM_NAME = "velox_mem_log"
# Where the log files can be found
DIR_PRE = "/home/user/log_files/"
# Subdirectories, in case you produced several measurements
DIR = ["m1", "m2", "m3"]

last_cpu_value = 0
final_cpu_results = []
final_mem_results = []


def process_entry(entry):
    global last_cpu_value

    if len(entry) != 3:
        return

    if entry[1] == "cpu_total":
        if last_cpu_value != 0:
            if long(entry[2]) - last_cpu_value < 0:
                return
            final_cpu_results.append(long(entry[2]) - last_cpu_value)

        last_cpu_value = long(entry[2])

    elif entry[1] == "mem_resident":
        final_mem_results.append(long(entry[2]) / (1024 * 1024))

# ##############
# MAIN PROCEDURE

system_fname_pattern = re.compile('.*(_cf_system_log).*')
log_pattern = re.compile('\[(.*?)\]')

try:
    os.mkdir('results')
except OSError:
    pass

for cur_dir in DIR:
    print "## Entering directory", DIR_PRE+cur_dir

    try:
        file_list = os.listdir(DIR_PRE+cur_dir)
    except OSError, e:
        print "Invalid directory:", DIR_PRE+cur_dir
        continue

    # Strip unnecessary files
    system_file_list = filter(lambda i: system_fname_pattern.search(i), file_list)

    if len(system_file_list) < 1:
        print "No files found in:", DIR_PRE+cur_dir
        continue

    print "Files:", system_file_list

    for item in system_file_list:
        print "## Reading", item
        with open(DIR_PRE+cur_dir+"/"+item, 'r') as tmp:
            for line in tmp:
                parts = log_pattern.findall(line)
                process_entry(parts)

        ## CPU

        max_measurement_key = len(final_cpu_results)
        split_factor = int(math.floor(max_measurement_key / 100))

        if split_factor > 1:
            final_sampled_results = [sum(final_cpu_results[i:i+split_factor])/split_factor for i in range(0, len(final_cpu_results), split_factor)]
        else:
            final_sampled_results = final_cpu_results

        max_measurement_key = 0
        if len(final_sampled_results) > max_measurement_key:
            max_measurement_key = len(final_sampled_results)

        file_counter = 0
        while os.path.exists("results/"+cur_dir+"_"+LOG_CPU_NAME+"_"+str(file_counter)):
            file_counter += 1

        final_sampled_results = array(final_sampled_results)
        final_sampled_results = final_sampled_results / float(max(final_sampled_results)) * 100
        with open("results/"+cur_dir+"_"+LOG_CPU_NAME+"_"+str(file_counter), "w") as f:
            tmp_str = ""
            f.write("cpu (%)"+"\r\n")
            for a in final_sampled_results:
                f.write(str(a)+"\r\n")

        try:
            os.remove('.gnutmp')
        except OSError:
            pass

        with open(".gnutmp", "w") as f:
            f.write("set term pngcairo\n")
            f.write("set datafile separator ','\n")
            f.write("set key autotitle columnhead\n")
            f.write("set output 'results/"+cur_dir+"_"+LOG_CPU_NAME+"_"+str(file_counter)+".png'\n")
            tmp_str = "plot 'results/"+cur_dir+"_"+LOG_CPU_NAME+"_"+str(file_counter)+"' u 1 w lines, '' u 2 w lines"
            f.write(tmp_str)

        os.system("gnuplot .gnutmp")

        ## MEM

        max_measurement_key = len(final_mem_results)
        split_factor = int(math.floor(max_measurement_key / 100))

        if split_factor > 1:
            final_sampled_results = [sum(final_mem_results[i:i+split_factor])/split_factor for i in range(0, len(final_mem_results), split_factor)]
        else:
            final_sampled_results = final_mem_results

        max_measurement_key = 0
        if len(final_sampled_results) > max_measurement_key:
            max_measurement_key = len(final_sampled_results)

        file_counter = 0
        while os.path.exists("results/"+cur_dir+"_"+LOG_MEM_NAME+"_"+str(file_counter)):
            file_counter += 1

        #final_sampled_results = array(final_sampled_results)
        #final_sampled_results = final_sampled_results / float(max(final_sampled_results)) * 100
        with open("results/"+cur_dir+"_"+LOG_MEM_NAME+"_"+str(file_counter), "w") as f:
            tmp_str = ""
            f.write("mem (mb)"+"\r\n")
            for a in final_sampled_results:
                f.write(str(a)+"\r\n")

        try:
            os.remove('.gnutmp')
        except OSError:
            pass

        with open(".gnutmp", "w") as f:
            f.write("set term pngcairo\n")
            f.write("set datafile separator ','\n")
            f.write("set key autotitle columnhead\n")
            f.write("set output 'results/"+cur_dir+"_"+LOG_MEM_NAME+"_"+str(file_counter)+".png'\n")
            tmp_str = "plot 'results/"+cur_dir+"_"+LOG_MEM_NAME+"_"+str(file_counter)+"' u 1 w lines, '' u 2 w lines"
            f.write(tmp_str)

        os.system("gnuplot .gnutmp")