import time
#import RPi.GPIO as GPIO
 
cps = 2			#characters per second
bps = 8 * cps

threshold_lo = 255
threshold_hi = 3*threshold_lo

def self_cal() :
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
	high = sample(raw)

	threshold_lo = (low + mid)/2
	threshold_hi = (mid + hi)/2

	GPIO.cleanup()

	return


def sample_raw():
	value = 0
	#read serial
	return value


def sample():
	value = sample(raw)
	if value < threshold_lo:
		return 0
	elif value < threshold_hi:
		return 1
	else:
		return 2

def run():
	print("High threshold: ", threshold_hi)
	print("Low threshold: ", threshold_lo)
	past_time =  ((time.time()*1000)%1000)
	index = 0

	while True:
		sample_time =  ((float) (index * 1000.0/bps + ((1000.0/bps)/2.0)))

		current_time  = ((time.time()*1000)%1000)
		if past_time < sample_time and sample_time <= current_time:
			#sample
			print("sampled between: ", str(past_time), current_time, "   target: ", sample_time, "   index: ", index)
			print((time.time()*1000)%1000)
			index = (index + 1) % bps
			print("new index ", index)
			time.sleep(0.01)



		past_time = current_time



	return

def main():
	#self_cal()

	run()




main() 