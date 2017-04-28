#!/usr/bin/python
import RPi.GPIO as GPIO
import time
# Use BCM numbering
GPIO.setmode(GPIO.BCM)
GPIO.setup(17, GPIO.IN)
while True:
    value = GPIO.input(17) 
    print value 
    time.sleep(1)