#!/bin/bash

wget http://mirror.pnl.gov/epel/7/x86_64/epel-release-7-5.noarch.rpm
rpm -ivh epel-release-7-5.noarch.rpm
rm -f epel-release-7-5.noarch.rpm
