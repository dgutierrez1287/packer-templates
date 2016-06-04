#!/bin/bash

wget https://apt.puppetlabs.com/puppetlabs-release-trusty.deb

dpkg -i puppetlabs-release-trusty.deb

apt-get update

apt-get install -y puppet puppetmaster facter
