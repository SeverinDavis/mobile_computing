import time
import RPi.GPIO as GPIO
 
bit_time = 0.02

threshold_lo = 0
threshold_hi = 1023

    return

def self_cal()
	GPIO.setmode(GPIO.BCM)
	GPIO.setup(23,GPIO.OUT) #sender1
	GPIO.setup(24,GPIO.OUT) #sender2

	GPIO.output(23, GPIO.LOW)
	GPIO.output(24, GPIO.LOW)
	time.sleep(bit_time)
	low = sample_raw()

	GPIO.output(23, GPIO.LOW)
	GPIO.output(24, GPIO.HIGH)
	time.sleep(bit_time)
	mid = sample_raw()

	GPIO.output(23, GPIO.HIGH)
	GPIO.output(24, GPIO.LOW)
	time.sleep(bit_time)
	mid =  (mid + sample_raw())/2

	GPIO.output(23, GPIO.HIGH)
	GPIO.output(24, GPIO.HIGH)
	time.sleep(bit_time)
	high =  sample(raw)

	threshold_lo = (low + mid)/2
	threshold_hi = (mid + hi)/2

	GPIO.cleanup()

	return


def sample_raw():
	value
	#read serial
	return value


def sample():
	value = sample(raw)
	if value < threshold_lo:
		return 0
	else if value < threshold_hi:
		return 1
	else
		return 2

def run():
	print("High threshold: ", threshold_hi)
	print("Low threshold: ", threshold_lo)
	while True:
		print("Running")
		time.sleep(1)
	return

def main():
	self_cal()

	run()




main() 