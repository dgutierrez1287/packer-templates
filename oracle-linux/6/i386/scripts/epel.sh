#!/bin/bash

wget http://mirror.pnl.gov/epel/6/i386/epel-release-6-8.noarch.rpm
rpm -ivh epel-release-6-8.noarch.rpm
rm -f epel-release-6-8.noarch.rpm
