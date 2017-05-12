import time
import RPi.GPIO as GPIO
import serial
 
cps = 1
bpc = 14		#characters per second
bps = cps*bpc

threshold_lo = 255
threshold_hi = 3*threshold_lo


array_codes = []
array_codes.append([1, 1])
array_codes.append([1, -1])


ser = serial.Serial(port='/dev/ttyACM0', baudrate = 115200)

def self_cal() :

	cal_wait = 0.01
	GPIO.setmode(GPIO.BCM)
	GPIO.setup(23,GPIO.OUT) #sender1
	GPIO.setup(24,GPIO.OUT) #sender2

	GPIO.output(23, GPIO.LOW)
	GPIO.output(24, GPIO.LOW)
	time.sleep(cal_wait)
	low = sample_raw()

	GPIO.output(23, GPIO.LOW)
	GPIO.output(24, GPIO.HIGH)
	time.sleep(cal_wait)
	mid = sample_raw()

	GPIO.output(23, GPIO.HIGH)
	GPIO.output(24, GPIO.LOW)
	time.sleep(cal_wait)
	mid =  (mid + sample_raw())/2

	GPIO.output(23, GPIO.HIGH)
	GPIO.output(24, GPIO.HIGH)
	time.sleep(cal_wait)	
	hi = sample_raw()

	print( (low + mid)/2)

	print((mid + hi)/2)

	GPIO.cleanup()

	#print("high threshold: ", threshold_hi)
	#print("low threshold: ", threshold_lo)

	return


def sample_raw():
	loop = True
	ser.flushInput()
	while loop:
		try:
			value = ser.readline()
			value = value.rstrip()
			loop = False
		except Exception as e:
			loop = True
	value = int(value)
	return value


def sample():
	value = sample_raw()
	#print(value)
	#print(threshold_lo)
	#print(threshold_hi)
	if value < threshold_lo: #both LEDS on
		return 2
	elif value < threshold_hi: #one LED on/ one off
		return 0
	else:
		return -2	#all off





def run():
	#print("High threshold: ", threshold_hi)
	#print("Low threshold: ", threshold_lo)
	past_time =  ((time.time()*1000)%1000)
	index = 0
	sub_index = 0
	sub_array = []
	array = []
	index_array = []
	while True:
		sample_time =  ((float) (index * 1000.0/bps + ((1000.0/bps)/2.0)))

		current_time  = ((time.time()*1000)%1000)
		if past_time < sample_time and sample_time <= current_time:
			value = sample()
			sub_array.append(int(value))
			index_array.append(index)
			#print("sampled between: ", str(past_time), current_time, "   target: ", sample_time, "   index: ", index)
			#print((time.time()*1000)%1000)
			index = (index + 1) % bps
			sub_index = index % 2
			if sub_index == 0:
				array.append(sub_array)
				sub_array = []
			#print("new index ", index)
		past_time = current_time

		if(len(array) == 7):
			#print(array)
			#print(index_array)
			print(decode(array, 0))
			print(decode(array, 1))
			index_array = []
			array = []


	return

def decode(channel, sender_id):
	Decoded_Message = []
	for i in range(len(channel)):
		for j in range(len(channel[i])-1):
			element = int(((channel[i][j]*array_codes[sender_id][0] + channel[i][j+1] * (array_codes[sender_id][1]))/2))
			if(element == 1):
				Decoded_Message.append(str(element))
			elif(element == -1):
				Decoded_Message.append("0")
		bit_array = ''.join(Decoded_Message)
		value = 0
		try:
			 value = (chr(int(bit_array,2)))
		except Exception as e:
			value = 0
	return value

def main():
	self_cal()
	GPIO.setmode(GPIO.BCM)
	GPIO.setup(23,GPIO.OUT) #sender1
	GPIO.setup(24,GPIO.OUT) #sender2

	GPIO.output(23, GPIO.LOW)
	GPIO.output(24, GPIO.LOW)
	run()




main() 