import RPi.GPIO as GPIO
import time
 
message = "HELLO WORLD FROM SENDER 1"

sender_id = 0
time_slice = 0.5
bit_time = 0.01

def send_bit(b):
	#print(b),
	time.sleep(bit_time)
	return
	

def send_char(c):
    print(c)
    bit_string = bin(ord(c))[2:].zfill(7)
    #print(bit_string)
    for x in range (0, len(bit_string)):
        send_bit(bit_string[x])
    return

GPIO.setmode(GPIO.BCM)
GPIO.setup(23,GPIO.OUT)
 


while True:
    for x in range (0, len(message)):
        current_time  = ((time.time()*1000)%1000)/1000.0
        if (sender_id*0.5) <= current_time and current_time < (sender_id*0.5 + 0.5) :
            #print("in window")
            if sender_id*0.5+0.5 - current_time > 8 * bit_time :
                #print("time")
                send_char(message[x])
        else :
            print(current_time)
            time.sleep(bit_time*10)
        #print("no time")
        			






while True:
	GPIO.output(23, GPIO.HIGH)
	time.sleep(1)
	GPIO.output(23, GPIO.LOW)
	time.sleep(1)



while True:
    for x in range (0, len(message)):
        #print(message[x])
        send_char(message[x])
        time.sleep(0.1)