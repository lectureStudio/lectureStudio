import { RtpPacketDecoder } from "./rtp-packet.decoder";
import { Action } from "../../action/action";
import { RtpPacket } from "../rtp-packet";
import { ProgressiveDataView } from "../../action/parser/progressive-data-view";
import { ActionParser } from "../../action/parser/action.parser";

class RtpActionDecoder implements RtpPacketDecoder<Action> {

	decode(packet: RtpPacket): Action {
		const dataView = new ProgressiveDataView(packet.payload.buffer);

		const length = dataView.getInt32();
		const type = dataView.getInt8();
		const timestamp = dataView.getInt32();

		const action = ActionParser.parse(dataView, type);
		action.timestamp = timestamp;

		return action;
	}

}

export { RtpActionDecoder };