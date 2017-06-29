class udpPacket(object):
	DISCOVERY_BROADCAST = "A"
	DISCOVERY_BROADCAST_RESPONSE = "B"
	LATENCY_COMMAND = "C"
	LATENCY_COMMAND_RESPONSE = "D"
	LATENCY_QUERY = "E"
	LATENCY_QUERY_RESPONSE = "F"
	DEFAULT = "G"
	DEFAULT_RESPONSE = "H"

	ROUTE_REQUEST = "I"
	ROUTE_REQUEST_RESPONSE = "J"


	odata = ""

	def __init__(self):
		self.odata = ""

	def setMessage(self, data):
		self.odata = data

	def buildMessage(self, messageID, destination, sender, messageType, messageData):
		self.odata = messageID + destination + sender + messageType + messageData

	def getMessage(self):
		return self.odata

	def getMessageID(self):
		return self.odata[0:8]

	def getDestination(self):
		return self.odata[8:11]

	def getSender(self):
		return self.odata[11:14]

	def getMessageType(self):
		return self.odata[14:15]

	def getMessageData(self):
		return self.odata[15:len(self.odata)]

	def getRoute(self):
		route = []
		if self.getMessageType() == self.ROUTE_REQUEST or self.getMessageType() == self.ROUTE_REQUEST_RESPONSE:
			count = int(self.odata[15:16])
			for x in range(0, count):
				route.append(self.odata[16+(x*3):16+(x*3)+3])
		return route

	def getLatencies(self):
		latencies = []
		if self.getMessageType() == self.LATENCY_COMMAND_RESPONSE:
			count = int(self.odata[15:16])
			for x in range(0, count):
				baseIndex = 16+(x*6)
				node = self.odata[baseIndex:baseIndex+3]
				latency = self.odata[baseIndex + 3: baseIndex + 6]
				latencies.append((node, latency))
		return latencies

	def appendLatency(self, node, latency):
		if self.getMessageType() == self.LATENCY_COMMAND_RESPONSE:
			latencies = self.getLatencies()
			latencies.append((node,latency))
			count = int(self.odata[15:16])
			count = count + 1
			latencyString = str(count)
			for x in range(0, count):
				latencyString = latencyString + str((latencies[x])[0]) + str((latencies[x])[1])
			self.odata = self.odata[0:15] + latencyString




	def print(self):
		print(self.getMessageData())



