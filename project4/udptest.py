import udppacket

test = udppacket.udpPacket()
test.setMessage("01234567999123AFFFFFFFFFFFFFFFFFFfF")

test.print()

print(test.getMessageID())
print(test.getDestination())
print(test.getSender())
print(test.getMessageType())
print(test.getMessageData())