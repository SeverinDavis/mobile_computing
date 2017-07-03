import common
import udppacket
import time

if __name__ == "__main__":
	txMessage = udppacket.udpPacket()
	
	# Discovery routine
	common.sock.settimeout(0.1)
	
	# Send the discovery broadcast 10 times, and hope all nodes will respond
	for x in range(0, 10) :
		txMessage.buildMessage(common.createRandomMid(), common.BROADCAST_IP, common.MY_IP, txMessage.DISCOVERY_BROADCAST, '')
		common.tx(txMessage.getMessage())
		# Wait for responses
		for x in range (0, 10):
			common.rx()
	
	# common.node_list = ['175', '193', '181', '177'] # TEST
	print('Discovery routine finished. Node list:')
	print(common.node_list)

	# Create adjacency matrix
	nodeCount = len(common.node_list) + 1 #shittyCode
	common.adjMatrix = [[0 for x in range(nodeCount)] for y in range(nodeCount)]

	# Latency command routine
	common.sock.settimeout(0.5)
	for x in range(0, 5) :
		for node_ip in common.node_list:
			txMessage.buildMessage(common.createRandomMid(), node_ip, common.MY_IP, common.LATENCY_COMMAND, '')
			common.tx(txMessage.getMessage())
			for x in range (0, 10):
				common.rx()
		# Do the query yourself	 
		print('Master is broadcasting latency query...')
		# Send a latency query to the neighbors
		query = udppacket.udpPacket()
		query.buildMessage(common.createRandomMid(), common.BROADCAST_IP, common.MY_IP, common.LATENCY_QUERY, '')
		common.sendTime = time.time()
		# Clear the neighbors list
		del common.neighbors[:]
		common.tx(query.getMessage())
		# Wait for some time for replies
		for x in range(0, 10) :
			common.rx()
		# Add the masters neighbors to the adj matrix too.
		for node in common.neighbors:
			x = common.findIndex(common.MY_IP)
			y = common.findIndex(node[0])
			if x == -1 or y == -1:
				print('-1 received')
				break
			common.adjMatrix[x][y] = node[1]
		# Print matrix
		common.printMatrix()