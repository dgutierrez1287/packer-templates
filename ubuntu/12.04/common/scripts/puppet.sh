#!/bin/bash

wget https://apt.puppetlabs.com/puppetlabs-release-precise.deb

dpkg -i puppetlabs-release-precise.deb

apt-get update

apt-get install -y puppet puppetmaster facter
