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
# List of the nodes in the network
node_list = []
# Adj. matrix
adjMatrix = []

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind(('0.0.0.0', 5008))
sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
sock.settimeout(0.1) # 0.1 second timeout

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

# For adjMatrix filling
def findIndex(ip):
	copy_list = node_list[:] #shittyCode
	copy_list.insert(0, MY_IP)
	for x in range(0, len(copy_list)):
		if copy_list[x] == ip:
			print(x)
			return x
	return -1


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
		print('Discovery response received.')
		# Check if the master node is reached
		if message.getDestination() == MY_IP :
			print('Sender: ' + message.getSender())
			# Check if the node list already contains the node
			for x in range(0, len(node_list)):
				if message.getSender() == node_list[x] :
					return
			print('Appending to the list...')
			node_list.append(message.getSender())
			print('Node list: ')
			print(node_list)
		# Forward the message to the destination
		print('Forwarding to master node...')
		tx(message.getMessage())

	elif message.getMessageType() == LATENCY_COMMAND :
		if message.getDestination() == MY_IP :
			print('Latency command received. Broadcasting latency query...')
			time.sleep(0.5) # TEST
			# Send a latency query to the neighbors
			query = udppacket.udpPacket()
			query.buildMessage(createRandomMid(), BROADCAST_IP, MY_IP, LATENCY_QUERY, '')
			tx(query.getMessage())
			# Wait for some time for replies
			for x in range(0, 10) :
				rx()
			# Send the latency list back to the master node
			latencyListReponse = udppacket.udpPacket()
			latencyListReponse.buildMessage(createRandomMid(), message.getSender(), MY_IP, LATENCY_COMMAND_RESPONSE, '0')
			for node in neighbors:
				latencyListReponse.appendLatency(node[0], node[1])
			tx(latencyListReponse.getMessage())
		else:
			print('Forwarding latency command to ' + message.getDestination())
			tx(message.getMessage())

	elif message.getMessageType() == LATENCY_COMMAND_RESPONSE :
		if message.getDestination() == MY_IP :
			print('Latency command response received.')
			print(message.getLatencies())
			# Add it to the adj. matrix
			latencyList = message.getLatencies()
			for node in latencyList:
				x = findIndex(message.getSender())
				y = findIndex(node[0])
				if x == -1 or y == -1:
					print('-1 received')
					return
				adjMatrix[x][y] = node[1]
			# Print matrix
			print('\n'.join([''.join(['  {:3}'.format(item) for item in row]) for row in adjMatrix]))
		else:
			print('Forwarding latency command response to ' + message.getDestination())
			tx(message.getMessage())

	elif message.getMessageType() == LATENCY_QUERY : 
		print('Latency query received. Replying...')
		time.sleep(0.1) # TEST
		# Just reply the sender
		reply = udppacket.udpPacket()
		reply.buildMessage(createRandomMid(), message.getSender(), MY_IP, LATENCY_QUERY_RESPONSE, '')
		tx(reply.getMessage())

	elif message.getMessageType() == LATENCY_QUERY_RESPONSE :
		if message.getDestination() == MY_IP :
			print('Latency query response received.')
			# Fill the list of neighbors
			for x in range(0, len(neighbors)):
				if (neighbors[x])[0] == message.getSender():
					return
			neighbors.append((message.getSender(), '123')) # TODO: calculatedLatency
			print(neighbors)

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