#!/bin/bash

wget http://download.fedoraproject.org/pub/epel/5/x86_64/epel-release-5-4.noarch.rpm
rpm -ivh epel-release-5-4.noarch.rpm
rm -f epel-release-5-4.noarch.rpm

