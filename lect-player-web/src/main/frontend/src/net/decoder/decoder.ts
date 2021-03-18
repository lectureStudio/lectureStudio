interface Decoder<T, R> {

	decode(input: T): R;

}

export { Decoder };