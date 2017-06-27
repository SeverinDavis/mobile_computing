### SENDER ###

import socket 
import fcntl
import struct
import datetime

UDP_IP = "192.168.210.255"
UDP_PORT = 5008
 
print "UDP target IP:", UDP_IP
print "UDP target port:", UDP_PORT
 
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM) # UDP
sock.bind(('0.0.0.0',5008))
sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

def get_ip_address(ifname):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    return socket.inet_ntoa(fcntl.ioctl(
        s.fileno(),
        0x8915,  # SIOCGIFADDR
        struct.pack('256s', ifname[:15])
    )[20:24])

l = []
nodes = []
latency = []
MYIP = get_ip_address('wlan0')
MYSHORTIP = MYIP[12: len(MYIP)]
global message_counter

print 'MYIP: ' + MYIP

print 'MYSHORTIP: ' + MYSHORTIP
sending_time = datetime.datetime.now();

def cache(mid, sender):
	l.append([mid,sender])
	while len(l) > 10:
		l.pop(0)


def search(mid, sender):
	for x in range(0, len(l)):
		if mid == (l[x])[0] and sender == (l[x])[1] :
			return 1
	return 0



def build_message(mid, destination, sender, message):
	return mid + destination + sender + message

def discovery():
	tx(build_message(str(message_counter).zfill(8), "255", MYSHORTIP, "discover"))
	global message_counter
	message_counter = message_counter + 1
	
def discover_sequence():
	del nodes[:]
	#send discovery message
	print 'Discovery request sent'
	discovery()
	sock.settimeout(1)

	#read 10 times/wait 10 seconds
	for x in range(0, 10):
		try:
			data, addr = sock.recvfrom(1024)
			sender = data[11:14]
			if sender != MYSHORTIP:
				print '192.168.210.' + sender + " responded to discovery request"
				nodes.append(sender)
		except socket.timeout:
			print 'Socket read timed out'

	print 'Discovered ' + str(len(nodes)) + ' nodes: '
	print nodes

def latency_sequence():
	del latency[:]

	for x in range(0, len(nodes)):
		global message_counter
		tx(build_message(str(message_counter).zfill(8), nodes[x], MYSHORTIP, "latency test"))
		sending_time = datetime.datetime.now()
		temp_mid = str(message_counter).zfill(8)
		print 'Sent latency request to ' + nodes[x] + ' at ' + str(sending_time)

		
		message_counter = message_counter + 1
		sock.settimeout(1)
		response = 0
		for y in range(0, 10):
			try:
				data, addr = sock.recvfrom(1024)
				receiving_time = datetime.datetime.now()
				sender = data[11:14]
				mid = data[0:8]
				if mid == temp_mid and sender == nodes[x]: 
					print 'Received latency response from ' + nodes[x] + ' at ' + str(receiving_time)
					nrtt = receiving_time - sending_time
					nlatency = nrtt/2
					print 'RTT: ' + str(nrtt)
					print 'Latency: ' + str(nlatency)
					latency.append(nlatency)
					response = 1
					break
			except socket.timeout:
				print 'Socket read timed out'

	latency.append(float('inf'))

	for x in range(0, len(nodes)):
		print 'Node ' + nodes[x] + ' latency: ' + str(latency[x])


def rx():
	data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
	print "received message:", data
	print 'intermediate: ', addr
	mid = data[0:8]
	print mid
	destination = data[8:11]
	print destination
	sender = data[11:14]
	print sender
	message = data[14: len(data)]
	print message
	destination_full = '192.168.210.' + destination
	print destination_full
	if destination_full == MYIP:
		print "Received message from ", sender, " : ", message
		latency = datetime.datetime.now() - sending_time
		print 'Latency: ' + str(latency.microseconds / 2)
	else : 
		if not search(mid, sender):
			cache(mid, sender)
			if destination == "255" and sender != MYSHORTIP:
				print "Discovery request from ", sender, " : ", message
				#TODO
				#TRANSMIT BACK TO SENDER TO RESPOND TO DISCOVERY REQUEST
				#FLOOD DISCOVERY REQUEST TO OTHER NODES
			else:
				tx(data)
				print 'forwarding...'


def tx(message):
	print 'Sending message: ' + message
  	sock.sendto(message, (UDP_IP, UDP_PORT))

if __name__ == "__main__":
	#tx('00000012181177hello!')
	#while(1):
	#	rx()
	global message_counter
	message_counter = 0
	discover_sequence()
	nodes.append("177")
	latency_sequence()
