

import time
import RPi.GPIO as GPIO
import multiprocessing
 
cps = 1
bpc = 14		#characters per second
bps = cps*bpc

sender_id = 0

SNDR_GPIO = sender_id + 23

sender_messages = []
sender_messages.append("HELLO FROM SENDER 1")
sender_messages.append("hello from sender 2")

array_s1 = []
array_s1.append([-1, -1])
array_s1.append([1, 1])

array_s2 = []
array_s2.append([-1, 1])
array_s2.append([1, -1])

array_codes = []
array_codes.append(array_s1)
array_codes.append(array_s2)

g_final_message = []

#print(array_codes)

def setup():
	GPIO.setmode(GPIO.BCM)
	GPIO.setup(SNDR_GPIO,GPIO.OUT) 
	return


def send_bit(b):
	if int(b) == 1:
		#print("1")																	
		GPIO.output(SNDR_GPIO, GPIO.HIGH)
	else:
		#print("0")
		GPIO.output(SNDR_GPIO, GPIO.LOW)


def encode(message):
	sender_convert = []
	for i in range(len(message)):
		sender_convert.insert(i, bin(ord(message[i]))[2:].zfill(7)	)
	sender_encoded = []
	for i in range(len(sender_convert)):
		for j in range(len(sender_convert[0])):
			sender_encoded.append(array_codes[sender_id][int(sender_convert[i][j])])	
	return sender_encoded

def run(final_message):
	past_time = ((time.time()*1000)%1000)
	index = 0
	sub_index = 0
	letter_index = 0

	while True:
		sample_time = (float) (index * 1000.0/bps)
		current_time = ((time.time()*1000)%1000)

		if index == 0: 
			if past_time > current_time:
				#print(letter_index*7, int(index/2), index%2)
				send_bit(final_message[letter_index*7+int(index/2)][index%2])
				#print("sent: ", final_message[letter_index*7+int(index/2)][index%2])
				#print("")
				#print("sent between: ", str(past_time), current_time, "   target: ", sample_time, "   index: ", index)
				#print((time.time()*1000)%1000)
				index = (index + 1) % bps
				#print("new index ", index)

		else :
			if past_time < sample_time and sample_time <= current_time:
				#print(letter_index*7, int(index/2), index%2)
				send_bit(final_message[letter_index*7+int(index/2)][index%2])
				#print("sent: ", final_message[letter_index*7+int(index/2)][index%2])
				#print("")
				#print("sent between: ", str(past_time), current_time, "   target: ", sample_time, "   index: ", index)
				#print((time.time()*1000)%1000)
				index = (index + 1) % bps
				if index%bpc == 0:
					letter_index = (letter_index + 1)% len(sender_messages[sender_id])
				#print("new index ", index)
		past_time = current_time

	return


def main():
	setup()
	g_final_message = (encode(sender_messages[sender_id]))
	#print(g_final_message)
	run(g_final_message)




main() 