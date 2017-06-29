import udppacket

test = udppacket.udpPacket()
test.setMessage("01234567999123D2345890999000")

test.print()

print(test.getMessageID())
print(test.getDestination())
print(test.getSender())
print(test.getMessageType())
print(test.getMessageData())

print(test.getLatencies())

test.appendLatency(666, 777)
print(test.getLatencies())

test.appendLatency(989, 898)
print(test.getLatencies())
