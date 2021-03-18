enum Side {

	TOP = "top",
	RIGHT = "right",
	BOTTOM = "bottom",
	LEFT = "left",
	NONE = "none"

}

class SideUtil {

	static valueOf(side: string): Side {
		return (<any>Side)[side.toUpperCase()];
	}

}

export { Side, SideUtil };