import udppacket
import random
import socket
import fcntl
import struct
import time

UDP_IP = "192.168.210.255"
UDP_PORT = 5008

DISCOVERY_BROADCAST = "A"
DISCOVERY_BROADCAST_RESPONSE = "B"
LATENCY_COMMAND = "C"
LATENCY_COMMAND_RESPONSE = "D"
LATENCY_QUERY = "E"
LATENCY_QUERY_RESPONSE = "F"
DEFAULT = "G"

ROUTE_REQUEST = "I"
ROUTE_REQUEST_RESPONSE = "J"

# Cache list
l = []

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind(('0.0.0.0', 5008))
sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
sock.settimeout(0.1) # 0.1 second timeout

def randomDelay():
	randomInt = random.randint(0,9)
	time.sleep(randomInt/100.0)

def createRandomMid():
	return str(random.randint(0,99999999)).zfill(8)


def get_ip_address(ifname):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    return socket.inet_ntoa(fcntl.ioctl(
        s.fileno(),
        0x8915,  # SIOCGIFADDR
        struct.pack('256s', ifname[:15])
    )[20:24])

# Get this node's IP
MYLONGIP = get_ip_address('wlan0')
MY_IP = MYLONGIP[12: len(MYLONGIP)]
BROADCAST_IP = '255'

def cache(mid, sender):
	l.append([mid,sender])
	while len(l) > 10:
		l.pop(0)


def search(mid, sender):
	for x in range(0, len(l)):
		if mid == (l[x])[0] and sender == (l[x])[1] :
			return 1
	return 0

def rx():
	try: 
		data, addr = sock.recvfrom(1024)
	except socket.timeout :
		return
	message = udppacket.udpPacket()
	message.setMessage(data)

	randomDelay()

	# Self check
	if message.getSender() == MY_IP :
		return

	# Cache check
	if not search(message.getMessageID(), message.getSender()):
		cache(message.getMessageID(), message.getSender())
	else :
		return

	# Message received
	if message.getMessageType() == ROUTE_REQUEST :
		print('Route request received.');
		if message.getDestination() == MY_IP :
			# Send route response
			response = udppacket.udpPacket()
			response.buildMessage(createRandomMid(), message.getSender(), MY_IP, ROUTE_REQUEST_RESPONSE, message.getMessageData())
			tx(response.getMessage())
		else :
			# Append yourself and forward
			print ('Route request is not for me. Forwarding...')
			message.appendNode(MY_IP)
			tx(message.getMessage())

	elif message.getMessageType() == ROUTE_REQUEST_RESPONSE :
		print('Route request response received.');
		route = message.getRoute()
		if message.getDestination() == MY_IP :
			# Print the route
			print('Sender:')
			print(MY_IP)
			print('Intermediate nodes:')
			print(route)
			print('Destination:')
			print(message.getSender())
		else :
			for node in route :
				if node == MY_IP :
					# Retransmit
					tx(message.getMessage())


	else :
		print('Default message type received.')
		if message.getDestination() == MY_IP :
			print ('Message is for me!')
			message.printMessageData()
		else :
			print ('Message is not for me. Forwarding...')
			tx(message.getMessage())

def tx(message):
	print 'Sending message: ' + message
  	sock.sendto(message, (UDP_IP, UDP_PORT))