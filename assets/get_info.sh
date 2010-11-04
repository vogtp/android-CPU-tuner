#!/bin/sh

echo "****************************************************************"
echo "******************  scaling_available_governo  *****************"
cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governo


echo "****************************************************************"
echo "********************* Kernel config  **************************"
gunzip< /proc/config.gz | grep CONFIG_CPU_FREQ