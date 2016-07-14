#!/bin/bash

sudo sed -i "s/^.*requiretty/#Defaults requiretty/" /etc/sudoers

sudo yum update -y

sudo yum install -y gcc make gcc-c++ kernel-devel-`uname -r` zlib-devel openssl-devel readline-devel sqlite-devel perl wget dkms nfs-utils man sudo curl vim-enhanced
