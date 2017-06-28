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

# Create an empty list of neighbors
neighbors = []
# Cache list
l = []
# Network list
network = []

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind(('0.0.0.0', 5008))
sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
# sock.settimeout(0.1) # 1 second timeout

def randomDelay():
	randomInt = random.randint(0,9)
	time.sleep(randomInt/100.0)

def randomTimeoutRX():
	randomInt = random.randint(0,9)
	sock.settimeout(randomInt/100.0)
	rx()
	sock.settimeout(0)

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

	# Severin's brilliant idea
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
	if message.getMessageType() == DISCOVERY_BROADCAST :
		print('Discovery broadcast received. Retransmitting... Intermediate: ' + str(addr))
		# Retransmit discovery broadcast
		tx(message.getMessage())
		# Send a response message back to the master node
		response = udppacket.udpPacket()
		response.buildMessage(createRandomMid(), message.getSender(), MY_IP, DISCOVERY_BROADCAST_RESPONSE, '')
		tx(response.getMessage())

	elif message.getMessageType() == DISCOVERY_BROADCAST_RESPONSE :
		print('Discovery response received. Forwarding to master node...')
		# Master check
		if message.getDestination() == MY_IP :
			network.append(message.getDestination())
			print('DISCOVERED: ' + message.getSender())
			return
		# Forward the message to the destination
		tx(message.getMessage())

	elif message.getMessageType() == LATENCY_COMMAND :
		print('Latency query received. Broadcasting latency query...')
		# Send a latency query to the neighbors
		query = udppacket.udpPacket()
		query.buildMessage(createRandomMid(), BROADCAST_IP, MY_IP, LATENCY_QUERY, '')
		tx(query.getMessage())
		# Wait for some time for replies
		for x in range(0, 5) :
			rx()
		# Send the latency list back to the master node
		latencyListReponse = udppacket.udpPacket()
		latencyListReponse.buildMessage(createRandomMid(), message.getSender(), MY_IP, LATENCY_COMMAND_RESPONSE, 'Neighbor list goes here') # TODO: serialize neighbor list
		tx(latencyListReponse.getMessage())

	elif message.getMessageType() == LATENCY_QUERY : 
		print('Latency query received. Replying...')
		# Just reply the sender
		reply = udppacket.udpPacket()
		reply.buildMessage(createRandomMid(), message.getSender(), MY_IP, LATENCY_QUERY_RESPONSE, '')
		tx(reply.getMessage())

	elif message.getMessageType() == LATENCY_QUERY_RESPONSE :
		print('Latency query reponse received.')
		# Fill the list of neighbors
		neighbors += {message.getSender(), 'some latency'} # TODO: calculatedLatency

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

if __name__ == "__main__":
	while(1):
		rx()