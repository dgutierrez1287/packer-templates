#!/bin/bash

wget http://mirror.clarkson.edu/epel/7/x86_64/e/epel-release-7-5.noarch.rpm
rpm -ivh epel-release-7-5.noarch.rpm
rm -f epel-release-7-5.noarch.rpm
