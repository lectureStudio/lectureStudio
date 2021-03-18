import { View } from "../view/view";
import { Command } from "../../command/command";

type CommandMethod = (command: Command<any>) => Promise<void>;

abstract class Presenter<T extends View> {

	private readonly cmdMethodMap: Map<string, CommandMethod> = new Map();

	protected readonly view: T;

	protected readonly parent: Presenter<View>;

	protected readonly children: Presenter<View>[];


	constructor(view: T, parent?: Presenter<View>) {
		this.view = view;
		this.parent = parent;
		this.children = [];
	}

	abstract initialize(): void;

	destroy(): void {
		this.destroyChildren();
	}

	protected destroyChildren(): void {
		while (this.children.length) {
			const child = this.children.shift();
			child.destroy();
		}
	}

	protected addChild(child: Presenter<View>): void {
		this.children.push(child);
	}

	protected execute(command: Command<any>): Promise<void> {
		const func = this.cmdMethodMap.get(command.constructor.name);

		if (func) {
			return func.call(this, command);
		}
		else if (this.parent) {
			return this.parent.execute(command);
		}

		return Promise.reject();
	}

	protected addCommandExecuter(name: string, method: CommandMethod): void {
		this.cmdMethodMap.set(name, method);
	}

}

export { Presenter };