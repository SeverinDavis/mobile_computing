import common
import udppacket
import sys

if __name__ == "__main__":
	message = udppacket.udpPacket()
	message.buildMessage(common.createRandomMid(), sys.argv[1], common.MY_IP, common.ROUTE_REQUEST, '0')
	common.tx(message.getMessage())
	for x in range(0, 50) :
		common.rx()
	print('Timeout.')