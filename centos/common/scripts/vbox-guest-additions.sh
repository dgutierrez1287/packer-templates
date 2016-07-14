#!/bin/bash

# Mount the disk image
cd /tmp
sudo mkdir /tmp/isomount
sudo mount -t iso9660 -o loop /root/VBoxGuestAdditions.iso /tmp/isomount

# Install the drivers
sudo /tmp/isomount/VBoxLinuxAdditions.run

# Cleanup
sudo umount isomount
sudo rm -rf isomount /root/VBoxGuestAdditions.iso
