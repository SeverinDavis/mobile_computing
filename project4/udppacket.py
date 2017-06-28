class udpPacket(object):
	DISCOVERY_BROADCAST = "A"
	DISCOVERY_BROADCAST_RESPONSE = "B"
	LATENCY_COMMAND = "C"
	LATENCY_COMMAND_RESPONSE = "D"
	LATENCY_QUERY = "E"
	LATENCY_QUERY_RESPONSE = "F"
	DEFAULT = "G"

	odata = ""

	def __init__(self):
		self.odata = ""

	def setMessage(self, data):
		self.odata = data

	def buildMessage(self, messageID, destination, sender, messageType, messageData):
		self.odata = messageID + destination + sender + messageType + messageData

    

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

	def print(self):
		print(self.odata)

