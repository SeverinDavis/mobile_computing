#!/usr/bin/python
import RPi.GPIO as GPIO
import time
time_slice = 0.5
bit_time = 0.02

# Use BCM numbering


def sample():
	#read GPIO
	array = []
	bit = 0
	i = 0
	while i < 7 :
		value = GPIO.input(17)
		value = 1- value
		array.append(str(value))

		#print value 
		#print(i, value,  " sample: ", ((time.time()*1000)%1000)/1000.0,)
		i = i + 1
		time.sleep(bit_time)
	
	bit_array = ''.join(array)
	#print(bit_array)
	#print(chr(int(bit_array,2)))


	return (chr(int(bit_array,2)))


def main() :
	GPIO.setmode(GPIO.BCM)
	GPIO.setup(17, GPIO.IN)
	senderlock = 1
	while True:
		current_time  = ((time.time()*1000)%1000)/1000.0
		if (0*0.5) <= current_time and current_time < (0*0.5 + 0.5) :
			if 0*0.5+0.5 - current_time > 8 * bit_time : 
				if senderlock == 1: #identifies sender1 entry and forces additional half bit_time wait.
					print("")
					senderlock = 0
					time.sleep(bit_time/2.0) #forces sample in the middle of bit_time
				#print("sender1")	
				print(sample()),

		if (1*0.5) <= current_time and current_time < (1*0.5 + 0.5) :
			if 1*0.5+0.5 - current_time > 8 * bit_time : 	
				if senderlock == 0:
					print("")
					senderlock = 1
					time.sleep(bit_time/2.0)
				#print("sender2")
				print(sample()),


main()

