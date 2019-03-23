public class CurrentPattern  {
	public int note, inst, vol, effect, effparam;
	
	public CurrentPattern(int[] incoming) {
		note = incoming[0];
		inst = incoming[1];
		vol = incoming[2];
		effect = incoming[3];
		effparam = incoming[4];
	}
	
}