interface Command<T> {

	execute(t: T): Promise<void>;

}

export { Command };