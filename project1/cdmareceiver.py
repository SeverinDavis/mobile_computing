import time
import RPi.GPIO as GPIO
import serial
 
cps = 1
bpc = 14		#characters per second
bps = cps*bpc

threshold_lo = 255
threshold_hi = 3*threshold_lo

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

	threshold_lo = (low + mid)/2
	threshold_hi = (mid + hi)/2

	GPIO.cleanup()

	print("high threshold: ", threshold_hi)
	print("low threshold: ", threshold_lo)

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
	if value < threshold_lo:
		return 2
	elif value < threshold_hi:
		return 0
	else:
		return -2





def run():
	print("High threshold: ", threshold_hi)
	print("Low threshold: ", threshold_lo)
	past_time =  ((time.time()*1000)%1000)
	index = 0
	sub_index = 0
	sub_array = []
	array = []
	while True:
		sample_time =  ((float) (index * 1000.0/bps + ((1000.0/bps)/2.0)))

		current_time  = ((time.time()*1000)%1000)
		if past_time < sample_time and sample_time <= current_time:
			value = sample()

			sub_array.append(int(value))
			
			print("sampled between: ", str(past_time), current_time, "   target: ", sample_time, "   index: ", index)
			print((time.time()*1000)%1000)
			index = (index + 1) % bps
			sub_index = (sub_index + 1) % 2
			if sub_index == 0:
				array.append(sub_array)
				sub_array = []
			print("new index ", index)
		past_time = current_time

		if(len(array) == 14):
			print(array)
			array = []





	return

def main():

	self_cal()
	run()




main() 