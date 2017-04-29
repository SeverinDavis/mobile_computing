import time
#import RPi.GPIO as GPIO
 
message = "HELLO WORLD FROM SENDER 1"

sender_id = 0
time_slice = 0.5
bit_time = 0.005

def send_bit(b):																		
	print(b, end='')																	#print sent bit
	#if b == 1:																			#if bit is 1, LED on
		#GPIO.output(23, GPIO.HIGH)														#signal high
	time.sleep(bit_time)																#wait bit_time for receiver to sample
	#GPIO.output(23, GPIO.LOW)															#signal low after bit_time delay
	return
	

def send_char(c):
    print(c + ": " , end='')															#print sent character
    bit_string = bin(ord(c))[2:].zfill(7)												#format bit_string to remove 0b and force length 7
    for x in range (0, len(bit_string)):												#loop through bit_string and transmit each bit
        send_bit(bit_string[x])															#send each bit
    print("")																			#new line after completed bit_string/character
    return


def main():
	#GPIO.setmode(GPIO.BCM)
	#GPIO.setup(23,GPIO.OUT)
	x = 0
	while True:
	    current_time  = ((time.time()*1000)%1000)/1000.0								#query time and convert to seconds in range [0, 1.0)
	    if (sender_id*0.5) <= current_time and current_time < (sender_id*0.5 + 0.5) : 	#check for valid time slice
	        if sender_id*0.5+0.5 - current_time > 8 * bit_time : 						#check if enough time to send single character
	            send_char(message[x])													#send single character
	            x = (x+1)%len(message)													#increment message index and mod to loop back


main() 

