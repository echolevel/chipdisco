import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import processing.core.*;
import sojamo.drop.*;
import controlP5.*;
import java.util.Properties;

import themidibus.*;
import crayolon.portamod.*;
import org.apache.commons.lang3.StringEscapeUtils;


/**
 * 
 * Chipdisco v6 by Brendan Ratliff - http://echolevel.co.uk
 * 
 * A dual-deck DJ application for mixing Amiga MOD files.
 * 
 * Based on IBXM by Martin Cameron (� 2008) 
 * 
 * What's changed? Loads of stuff, particularly:
 * 
 * 2019 Fixes:
 * # The core.jar include from Processing changes the way selectInput works from around v 2.x, so I've updated
 * 	 the functions that use it.
 * # I might also unleash this abomination of a codebase upon GitHub for posterity, fully prepared for the fact
 *   that lots of wise and smart people might come along and say "why don't you do it like _this_?" or "could
 *   you implement unit testing using such-and-such a fancy dev paradigm that you've never heard of?". If they 
 *   do, I'll say "No. But if you'd like to, feel free."
 * 
 * # NO MORE SINGLE/UNIFIED PLAYLIST! 
 *   This is a good idea for most MP3 DJ apps, but was baffling unintuitive for Chipdisco.
 * 
 * # GUI SIZE INCREASED BY 150%!
 *   It was difficult to see on most screens and now it fills a 1280x800 screen (like my Macbook's screen, for instance) perfectly
 * 
 * # NO MORE BROWSER APPLET VERSION! 
 *   Processing are ditching support for this and applet security keeps getting tighter and fiddlier. And Chipdisco doesn't *need* to be a browser app, let's face it.
 * 
 * # NO PRE-LOADED PLAYLIST and PLAYLIST IMPROVEMENTS!
 *   The playlist has been greatly improved although some small things have been lost - the ability to remove items,
 *   for instance, is temporarily gone. Overall, though, it's more solid. M3U loading and saving is still there,
 *   but I've removed all the URL-based stuff that existed in the (pointless) browser version. Also, you now need
 *   to make sure that your playlists don't contain metadata lines (beginning with '#') - this can be done easily
 *   in a text editor, and Chipdisco saves its M3U files properly formatted. M3U files are just text files with
 *   file paths on separate lines. Chipdisco normally needs fully-qualified paths (e.g. /Users/yourname/Mods/file.mod or
 *   C:/Mods/file.mod) but I suppose it should work with relative paths too - I haven't tried that yet. M3U files
 *   saved from Chipdisco will always reflect the filepath as it was added (by file or by loading an M3U). Oh,
 *   and you can now scroll big playlists with the mousewheel/trackpad - hooray!
 *   
 * # CONFIG FILE!
 *   Simple, but great - Chipdisco now looks for a file called chipdisco_config.txt in your user home directory.
 *   A template is included in the Chipdisco distribution - just copy or move it to your homedir. It's annotated
 *   with explanations of various things. I'll try to keep current parameters the same in future updates to
 *   Chipdisco so as not to ruin your old configs. Chipdisco will never write to this config file or overwrite
 *   it entirely, but it's probably still a good idea to keep a backup. Amongst other things, this file lets you
 *   do things like define startup playlists based on M3U files or directories full of modules, define your MIDI
 *   device choices (once you've discovered in Chipdisco which numbers your chosen devices have) and defining your
 *   preferred choice of crossfader curve.  
 *  
 * 
 * # NO MORE MULTIPLE SOUNDCARD SUPPORT!
 *   As well as being a potential source of crashes at the best of times, this was one of the many things that 
 *   Apple's notoriously declinatory rendered unusable. The solution is a HUGE tradeoff...but one I'm quite happy
 *   with. Now, all 4-channel MODs are fully panned right for cue-mix or left for FOH playout. XMs with panning
 *   envelopes or pattern pan commands go haywire with this, which effectively rules out most XMs for DJ-style
 *   playback. I might add a button to disable this functionality so Chipdisco can be used as a straightforward
 *   player, but hey, there are other - better - mod players available for that :) So it means that while Chipdisco
 *   can now be used for DJing with only a splitter cable from a single soundcard (and only using a single mono
 *   channel in a venue's mixing desk), it can pretty much only be used for modules without in-pattern pan commands.
 *   
 * # ER, HOW IS THAT A TRADEOFF? WHAT ARE THE BENEFITS?
 *   Well, the fact is...Cue Mix never really worked in the first place. It often caused crashes, particularly
 *   when Java implementations on differing platforms/versions failed to properly acquire audio devices at the 
 *   OS level. And on top of that, because of the unavoidable timing glitches introduced during the audio device switch 
 *   due to having to empty one buffer and fill another, when you synced up the perfect beatmatch and then switched
 *   the second tune from your headphones to the main playout...it was out of sync! Which completely defeated the
 *   point of the feature and made it useless. 
 *   
 *   Now, though, we're not changing audio devices - we're simply adjusting the panning on two MODs that are playing
 *   through the same interface. Now we can do all that syncing, crossfading, cue-mixing etc. simply through pan
 *   and volume controls - which are simply hooks into the patterndata as it's processed, basically introducing
 *   artificial per-note volume and panning commands right before the whole thing is mixed and sent to the audio
 *   interface. It's CPU-light, it never causes crashes, and it will work on any machine capable of running Java
 *   apps and playing sound. Brilliant!
 *   
 * # NO XM DJING? REALLY? 
 *   Not *none*, but you'll find that XMs with pan envelopes on instruments or channel pan commands (which is 
 *   most of them) will not submit to my pan-mixing trick. They'll fight back and lots of notes will leak into
 *   the wrong channel. However, some few XMs might work, and you can probably also edit XMs to make them work - 
 *   probably by using Milkytracker to strip out all pan commands/envelopes. Obviously it might completely ruin
 *   the module's sound and be a huge timewaster (unless I write an app that batch-strips pan instructions out
 *   of XM files, but that's obnoxious and destructive so I won't). One last thing I could - and might - do is 
 *   to just try and strip that stuff out at the module load time in Chipdisco. Watch this space...
 *   
 * # OTHER STUFF!
 * 	 Loads of little changes, including the removal of the 'spectrum' display (which was never a true spectrum
 *   but was close enough), to optimise performance. The sourcecode has been reduced from about 3000 lines to 
 *   about 2000 lines, which is enormously cathartic.
 *   
 * # HOW TO USE THE NEW FEATURES
 *   As I've explained, cue mixing now works differently but so does volume. By default, modules - when
 *   double-clicked - are loaded into the deck and played in CUE mode, which should be audible through 
 *   the RIGHT stereo channel of your computer's audio output. If you're wearing headphones, you should hear
 *   this on the right; if you're set up using a splitter cable, you should get this in mono through your headphones
 *   while the mono feed to the mixing desk is silent.
 *   
 *   In CUE mode, moving this deck's volume fader won't do anything, nor will moving the crossfader. Adjust the
 *   CUE mode volume using the - you guessed it - CUE MIX VOLUME knob! Due to the weird library I use for knobs
 *   and sliders, you have to click and move this horizontally to turn it. There is also a MIDI mapping for this 
 *   control, which I choose to assign to the knob on channel 9 of my Korg nanoKontrol. CUE MIX VOLUME applies
 *   to any module played in CUE mode on either deck.
 *   
 *   The button with the speaker icon changes a deck to FOH mode. In FOH, or non-CUE mode (FOH stands for 'front
 *   of house', which you can think of as what is heard over the PA system in a venue), the volume faders DO 
 *   control the decks' volume. They also act as a global cap for each deck, so that the crossfader will never
 *   bump a deck's volume beyond that set by the fader. This is all standard stuff for a DJ mix setup, but I'm
 *   explaining it in detail because Chipdisco didn't previously work like this!
 *   
 * # A NOTE ON VOLUME
 *   Sharp readers will be wondering whether shoving two modules, each in MONO, down a single MONO audio channel
 *   could cause a bit of distortion...and they'd be right. At the cost of some volume resolution, all modules
 *   are now volume-capped at 48/64, if you imagine 64 as being the full volume of a module (C40 in the pattern).
 *   That's 3/4 of the default playout volume, but you can crank things up on the mixer, possibly use a headphone
 *   amp if you *really* need to, and you shouldn't suffer unwanted distortion even if beatmatching two modules
 *   comprised solely of square waves :)
 *   
 * # OTHER NOTES/CAUTIONS
 *   I'll add these as I think of them. One thing: drag'n'drop to add tunes may cause a tiny gap in playback, which
 *   you probably don't want during live performance. Using the Add button should be fine, but the best thing is
 *   always to have prepared a playlist in advance
 * 
 * # TO DO!
 *   I need to fix some keyboard mappings and possibly some of the MIDI assignments. I also need to experiment
 *   with brutally forcing all XMs and S3Ms to hard-pan left or right (as discussed above) - until I do, Chipdisco
 *   is basically an Amiga MOD DJ app rather than a MOD/XM/S3M DJ app! Oh yeah, and I need to get the JVM
 *   exit hook to work so I can get rid of that awful glitch sound of the buffer emptying when the program is
 *   ended. Meanwhile, if you don't like it, just pause both decks before quitting :)
 * 
 * # DEV NOTES:
 *   For some reason, the Processing application building plugin I'm using sets LSUIPresentationMode on OS X 
 *   to 4, which hides all OS UI elements (like the Dock and the menubar). This will almost always be unwanted
 *   so at the moment, I'm manually adjusting the Mac app's Info.plist to 0, which shows everything. If I forget
 *   to do this on a release build, that's how to fix it... 
 * 
 */

public class Chipdisco6 extends PApplet {

	private static final long serialVersionUID = 1L;
	
	//int width = 854; //reduced by 170 2.08.12
	//int height = 420;
	int width = 1280;
	int height = 630;
	
	PImage cdlogo;
	PImage credits;
	
	float doubleclick = 0;
	float doublerightclick = 0;
	boolean[] chanmutes = new boolean[32];
	boolean[] chanmutesB = new boolean[32];
	boolean channelbars = true, channelbarsB = true, foundprops = false, keydown = false;
	Properties props;
	Properties midiprops;
	PortaMod deckA, deckB;	
	ControlButton bpmgfxA, bpmgfxB, crossfadergfx, playbt, pausebt, cuebt, loopbt, emptybt, syncbt, incbpmbt, decbpmbt, mutebt, playbtB, pausebtB, cuebtB, 
	loopbtB, emptybtB, syncbtB, incbpmbtB, decbpmbtB, mutebtB, volgfxA, volgfxB, transpgfx, transpgfxB, infodisplaygfx, infodisplaygfxB, playlistgfx;
	
	/*boolean joyselected = false;
	ControllIO controll;
	ControllDevice joystick;
	ControllStick stick;
	ControllButton button1, button2, button3, button4;
	int joyX, joyY;
	boolean[] joybuttons;*/
	
	int[] liveinstrument = {0, 0};
	int jam = 0, sequencecounter = 0, rowhighlight = 0;
	int[] chanpalette = { color(0,174,217), color(83,219,63), color(255,232,26), color(255,153,58), color(255,83,91), color(255,23,163), 
			  color(124,92,203), color(52,182,229), color(204,9,47), color(216,0,115), color(0,175,77), 
			  color(124,92,203), color(0,174,217), color(83,219,63), color(255,232,26), color(255,153,58), color(255,83,91), color(255,23,163), 
			  color(124,92,203), color(52,182,229), color(204,9,47), color(0,37,150), color(216,0,115), color(0,175,77), 
			  color(124,92,203),color(0,174,217), color(83,219,63), color(255,232,26), color(255,153,58), color(255,83,91), color(255,23,163), color(83,219,63)};
	int[] blockpalette = { color(255,23,163), color(0,174,217), color(83,219,63), color(255,232,26), color(255,153,58), color(255,83,91),  
			  color(124,92,203), color(52,182,229), color(204,9,47), color(216,0,115), color(0,175,77), 
			  color(124,92,203), color(0,174,217), color(83,219,63), color(255,232,26), color(255,153,58), color(255,83,91), color(255,23,163), 
			  color(124,92,203), color(52,182,229), color(204,9,47), color(0,37,150), color(216,0,115), color(0,175,77), 
			  color(124,92,203),color(0,174,217), color(83,219,63), color(255,232,26), color(255,153,58), color(255,83,91), color(255,23,163), color(83,219,63)};
	int[] shadepalette = { color(14,196,239), color(94,239,74), color(255,239,111), color(255,179,111), color(255,111,120), color(255,63,183), color(150,111,255),
			color(63,205,255), color(239,29,72), color(239,14,139)};
	boolean colourcycle = false;
	//int pausedseqB, pausedrowB, pausedseq, pausedrow, specheight, b_specheight, tempovalue, totaltime, seconds, secondsB, buffersize; //totaltime: duration of main tune in seconds, seconds: progress of main tune in seconds	
	int volvalue = 127;
	int paused = 2; // 0 is unpaused, 1 is paused, 2 is our initial value.
	int pausedB = 2; // 0 is unpaused, 1 is paused, 2 is our initial value.

	int foregroundcol = chanpalette[0];
	int foreshadecol = shadepalette[0];
	int crossfaderX, crossfaderY, crossfaderW, crossfaderH, crossfade = 0;
	
	//and vars for those inputs:
	int midiIndevice = -1;
	int midiIndevice2 = -1;
	int midiNotedevice = -1;
	int cuevolume = 48;
	boolean fastforwardrow = false;
	boolean rewindrow = false;
	boolean fastforwardrowB = false;
	boolean rewindrowB = false;
	boolean altbutton = false;
	int transpose;
	int transposeB;
	//Skinv vars Deck A

	int infodisplayX = 17;
	int infodisplayY = 300;
	int infodisplayW = 393;
	int infodisplayH = 290;
	int spectrumX = 15, spectrumY = 48, spectrumW = 660, spectrumH = 180;

	int loopdivision = 4;
	//skin vars Deck A end

	//skin vars Deck B
	int b_infodisplayY = 300;
	int b_infodisplayW = 393;
	int b_infodisplayH = 290;
	int b_infodisplayX = width - b_infodisplayW - 10;	
	int b_loopdivision = 4;
	//skin vars Deck B end

	int volumelocal = 48, volumelocalB = 48, crossbehaviour = 0, currentsong = 0, currentsongB = 0;
	int loopcaught, loopstart, loopend, loopcaughtB, loopstartB, loopendB, totalrows, endcount = 0; 
	float loopcount, loopcountB;
	boolean tempoknoblock = false, tempoknoblockB = false,  transposeknoblock = false, transposeknoblockB = false;
	boolean playing = false, muted = false, mutedB = false, shiftpressed = false, ctrlpressed = false, altpressed = false, loopcurrentsegment = false, loopcurrentsegmentB = false;	
	boolean[] chansolo = new boolean[32], chansoloB = new boolean[32];
	String title = "";
	int bpmvalue = 0;
	//String textstring, titlestring = "ChipdiscoDJ", aboutstring = "by Brendan Ratliff (aka Syphus) - http://echolevel.co.uk";
	String textstring, titlestring = "", aboutstring = "";
	String[] infotext;
	PFont syntaxterror32, modenine10, helvetica11, helvetica15, helvetica20;
	FileInputStream file_input_stream;
	InputStream mymod;
	SDrop drop;
	ChipdiscoDropListenerA droplistenerA;
	ChipdiscoDropListenerB droplistenerB;
	ControlP5 controlp5;
	MidiBus myBus, myBus2, noteInputbus;
	String ControlBusname, NoteBusname;
	boolean deckAcue = true, deckBcue = true; //always start in cue mix mode - headphones rather than FOH
	int xcurveProp, midinoteProp, slicesProp, midiInput2Prop;
	String deckAdir = "", deckBdir = "";
	String selectedTuneA = "", selectedTuneB = "";
	int playlistposA = 0;
	int playlistposB = 0;

	//Deck B
	String[] infotextB;
	int totaltimeB, endcountB = 0;
	boolean playingB = false;
	String titleB = "";
	int bpmvalueB = 0;
	ListBox midi2, midi3, midi4, filemenu, playlistCP5_a, playlistCP5_b, crossfaderbehaviour, slicebehaviour, joysticklist;	
	Slider volumesliderA, volumesliderB, crossfadeslider, temposliderA, temposliderB, translideA, translideB;	
	//Slider cuevolumeslider;
	Knob cuevolumeslider;
	Button listAadd, listAremove, listAaddurl, listAloadm3u, listAsavem3u, listBadd, listBremove, listBaddurl, listBloadm3u, listBsavem3u;	
	MultiList menutree;
	ControlGroup<Group> plcontrolgroup, plcontrolgroupB;
	int intTempoA, intTempoB, intVolumeA, intVolumeB, intCuevolume, intCrossfade, intTransposeA, intTransposeB;
	
	int slicetargets[] = new int[32];
	int slicetargetsB[] = new int[32];
	int slicetriggerbehaviour = 1;
	boolean cpuSaver;	
	boolean deckAslavesync = false;
	boolean deckBslavesync = false;
	
	//Default MIDI mappings (Korg nanoKontrol)
	int ccCrossfader=13;
	int ccCuemixvol=22;
	int ccCuemixA=41;
	int ccCuemixB=31;
	int ccAlt=46;
	int ccPauseA=49;
	int ccPauseB=44;
	int ccSyncA=47;
	int ccSyncB=48;
	int ccForcesync=45;
	int ccTempoA=14;
	int ccTempoB=18;
	int ccTransposeA=16;
	int ccTransposeB=20;
	int ccSeekA=17;
	int ccSeekB=21;
	int ccVolA=15;
	int ccVolB=19;
	int ccResetTempoA=23;
	int ccResetTempoB=27;
	int ccPattLoopTrigA=24;
	int ccPattLoopTrigB=28;
	int ccPattLoopTogA=25;
	int ccPattLoopTogB=29;
	int ccChanvolA1=2;
	int ccChanvolA2=3;
	int ccChanvolA3=4;
	int ccChanvolA4=5;
	int ccChanvolB1=6;
	int ccChanvolB2=8;
	int ccChanvolB3=9;
	int ccChanvolB4=12;
	int ccChanmuteA1=33;
	int ccChanmuteA2=34;
	int ccChanmuteA3=35;
	int ccChanmuteA4=36;
	int ccChanmuteB1=37;
	int ccChanmuteB2=38;
	int ccChanmuteB3=39;
	int ccChanmuteB4=40;
	
	
	public void setup() {
		size(1280,630);
		frameRate(30);
		noSmooth();
		//deckA = new PortaModJava(this.getClass(), neA);
		//deckB = new PortaModJava(this.getClass(), neB);
		deckA = new PortaMod(this);
		deckA.mono = 2;
		deckB = new PortaMod(this);
		deckB.mono = 2;
		cdlogo = loadImage("logo.png");
		credits = loadImage("credit.png");
		for (int i=0; i < slicetargets.length; i++) {
			slicetargets[i] = 0;
			slicetargetsB[i] = 0;
		}
		//controll = ControllIO.getInstance(this);
		crossfaderW = 384;
		crossfaderH = 77;
		crossfaderX = (width/2)-(crossfaderW/2);
		crossfaderY = 300;

		for (int i=0; i < chanmutes.length; i++ ) {
			chanmutes[i] = false;
			chanmutesB[i] = false;
		}
		
		try {
			props = new Properties();
			props.load(new FileInputStream(System.getProperty("user.home") + "/chipdisco_config.txt"));
			deckAdir = props.getProperty("deckAdir", "./");
			deckBdir = props.getProperty("deckBdir", "./");
			xcurveProp = Integer.parseInt(props.getProperty("xcurve", "0"));
			crossbehaviour = xcurveProp;
			midinoteProp = Integer.parseInt(props.getProperty("midiNote", "0"));
			midiInput2Prop = Integer.parseInt(props.getProperty("midiInput2", "0"));
			slicesProp = Integer.parseInt(props.getProperty("slices", "0"));	
			if(Integer.parseInt(props.getProperty("cpuSaver", "0")) > 0) {
				cpuSaver = true;
			} else {
				cpuSaver = false;
			}
			foundprops = true;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			midiprops = new Properties();
			midiprops.load(new FileInputStream(System.getProperty("user.home") + "/chipdisco_midimap.txt"));

			ccCrossfader= Integer.parseInt(midiprops.getProperty("mccCrossfader"));
			ccCuemixvol= Integer.parseInt(midiprops.getProperty("mccCuemixvol"));
			ccCuemixA= Integer.parseInt(midiprops.getProperty("mccCuemixA"));
			ccCuemixB= Integer.parseInt(midiprops.getProperty("mccCuemixB"));
			ccAlt= Integer.parseInt(midiprops.getProperty("mccAlt"));
			ccPauseA= Integer.parseInt(midiprops.getProperty("mccPauseA"));
			ccPauseB= Integer.parseInt(midiprops.getProperty("mccPauseB"));
			ccSyncA= Integer.parseInt(midiprops.getProperty("mccSyncA"));
			ccSyncB= Integer.parseInt(midiprops.getProperty("mccSyncB"));
			ccForcesync= Integer.parseInt(midiprops.getProperty("mccForcesync"));
			ccTempoA= Integer.parseInt(midiprops.getProperty("mccTempoA"));
			ccTempoB= Integer.parseInt(midiprops.getProperty("mccTempoB"));
			ccTransposeA = Integer.parseInt(midiprops.getProperty("mccTransposeA"));
			ccTransposeB = Integer.parseInt(midiprops.getProperty("mccTransposeB"));
			ccSeekA= Integer.parseInt(midiprops.getProperty("mccSeekA"));
			ccSeekB= Integer.parseInt(midiprops.getProperty("mccSeekB"));
			ccVolA= Integer.parseInt(midiprops.getProperty("mccVolA"));
			ccVolB= Integer.parseInt(midiprops.getProperty("mccVolB"));
			ccResetTempoA= Integer.parseInt(midiprops.getProperty("mccResetTempoA"));
			ccResetTempoB= Integer.parseInt(midiprops.getProperty("mccResetTempoB"));
			ccPattLoopTrigA= Integer.parseInt(midiprops.getProperty("mccPattLoopTrigA"));
			ccPattLoopTrigB= Integer.parseInt(midiprops.getProperty("mccPattLoopTrigB"));
			ccPattLoopTogA= Integer.parseInt(midiprops.getProperty("mccPattLoopTogA"));
			ccPattLoopTogB= Integer.parseInt(midiprops.getProperty("mccPattLoopTogB"));
			ccChanvolA1= Integer.parseInt(midiprops.getProperty("mccChanvolA1"));
			ccChanvolA2= Integer.parseInt(midiprops.getProperty("mccChanvolA2"));
			ccChanvolA3= Integer.parseInt(midiprops.getProperty("mccChanvolA3"));
			ccChanvolA4= Integer.parseInt(midiprops.getProperty("mccChanvolA4"));
			ccChanvolB1= Integer.parseInt(midiprops.getProperty("mccChanvolB1"));
			ccChanvolB2= Integer.parseInt(midiprops.getProperty("mccChanvolB2"));
			ccChanvolB3= Integer.parseInt(midiprops.getProperty("mccChanvolB3"));
			ccChanvolB4= Integer.parseInt(midiprops.getProperty("mccChanvolB4"));
			ccChanmuteA1= Integer.parseInt(midiprops.getProperty("mccChanmuteA1"));
			ccChanmuteA2= Integer.parseInt(midiprops.getProperty("mccChanmuteA2"));
			ccChanmuteA3= Integer.parseInt(midiprops.getProperty("mccChanmuteA3"));
			ccChanmuteA4= Integer.parseInt(midiprops.getProperty("mccChanmuteA4"));
			ccChanmuteB1= Integer.parseInt(midiprops.getProperty("mccChanmuteB1"));
			ccChanmuteB2= Integer.parseInt(midiprops.getProperty("mccChanmuteB2"));
			ccChanmuteB3= Integer.parseInt(midiprops.getProperty("mccChanmuteB3"));
			ccChanmuteB4= Integer.parseInt(midiprops.getProperty("mccChanmuteB4"));
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		colorMode(RGB);
		syntaxterror32 = loadFont("SyntaxError-32.vlw");		
		modenine10 = loadFont("modenine-10.vlw");
		helvetica11 = loadFont("HelveticaNeue-Medium-11.vlw");
		helvetica15 = loadFont("HelveticaNeue-Medium-15.vlw");
		helvetica20 = loadFont("HelveticaNeue-Medium-20.vlw");
		ControlFont controlfont = new ControlFont(helvetica15, 15);
		smooth();		
		for (int i=0; i<height; i++) {
			stroke(map(i, 0, height, 450, 765));
			line(0,i,width,i);
		}
		fill(49,51,46);
		rect(0,0,width,height);
		
		controlp5 = new ControlP5(this);
		controlp5.setMoveable(false);
		controlp5.setFont(controlfont);

		//name, float min, float max, float default, X, Y, W, H
		volumesliderA = controlp5.addSlider("intVolumeA", 0, 48, 48, width/2 - 72 , 45 , 38 , 174);
		volumesliderA.setCaptionLabel("");
		volumesliderA.getValueLabel().setVisible(false);
		volumesliderA.setSliderMode(0);
		volumesliderA.setColorBackground(0xff31332e);
		
		volumesliderB = controlp5.addSlider("intVolumeB", 0,48,48,width/2 + 35, 45 , 38, 174);
		volumesliderB.setCaptionLabel("");
		volumesliderB.getValueLabel().setVisible(false);
		volumesliderB.setSliderMode(0);
		volumesliderB.setColorBackground(0xff31332e);
		
		/*
		cuevolumeslider = controlp5.addSlider("cuemixvolume", 0, 48, cuevolume, width/2 + 35, 450, 38, 120);
		cuevolumeslider.setCaptionLabel("");
		cuevolumeslider.getValueLabel().setVisible(false);
		cuevolumeslider.setSliderMode(0);
		cuevolumeslider.setColorBackground(0xff31332e);*/
		
		cuevolumeslider = controlp5.addKnob("cuemixvolume")
									.setRange(0, 48)
									.setValue(48)
									.setValue(cuevolume)
									.setRadius(40)
									.registerTooltip("Set volume level for the cue mix function")
									.setCaptionLabel("Cue Mix Volume")
									.setViewStyle(Knob.ARC)
									.setDragDirection(Knob.HORIZONTAL)
									//.setColorBackground(0xff31332e)
									.setColorBackground(0xff42443f)
									//.showTickMarks()
									.hideTickMarks()
									.setScrollSensitivity(13.9f)
									//.setNumberOfTickMarks(16)
									.setPosition(width/2 - 36, 514);
									;
		
		
		translideA = controlp5.addSlider("intTransposeA", 0, 24, 12, width/2 - 26, 89, 20, 87);
		translideA.getCaptionLabel().setVisible(false);
		translideA.getValueLabel().setVisible(false);
		translideA.setSliderMode(0);
		translideA.setColorBackground(0xff31332e);
		//translideA.showTickMarks(true);
		//translideA.setNumberOfTickMarks(24);
		//translideA.snapToTickMarks(true);
		
		translideB = controlp5.addSlider("intTransposeB", 0, 24, 12, width/2 + 5, 89, 20, 87);
		translideB.getCaptionLabel().setVisible(false);
		translideB.getValueLabel().setVisible(false);
		translideB.setSliderMode(0);
		translideB.setColorBackground(0xff31332e);
		//translideB.showTickMarks(true);
		//translideB.setNumberOfTickMarks(24);
		//translideB.snapToTickMarks(true);
		
		//This is updated with the module's initial tempo, whenever a module is loaded.
		temposliderA = controlp5.addSlider("intTempoA", 32, 255, 125, 80,  242, 210, 38);
		temposliderA.getCaptionLabel().getStyle().marginLeft = -186;
		temposliderA.getCaptionLabel().setColor(0xffffffff);
		temposliderA.setCaptionLabel(" bpm");
		temposliderA.getCaptionLabel().toUpperCase(false);
		temposliderA.setColorValueLabel(0xffffffff);
		temposliderA.setColorBackground(0xff31332e);
		temposliderA.setSliderMode(0);
		
		/*tempodecA = controlp5.addButton("tempoincA", 0, infodisplayX, spectrumY+spectrumH + 20, 20, 20).setSwitch(true);
		tempodecA.captionLabel().setColor(0);
		tempodecA.setCaptionLabel("-");
		tempoincA = controlp5.addButton("tempodecA", 0, infodisplayX+infodisplayW - 20, spectrumY+spectrumH+20, 20, 20).setSwitch(true);
		tempoincA.captionLabel().setColor(0);
		tempoincA.setCaptionLabel("+");*/
				
		temposliderB = controlp5.addSlider("intTempoB", 32, 255, 125, 999,  242, 210, 38);
		temposliderB.getCaptionLabel().getStyle().marginLeft = -124;
		temposliderB.getCaptionLabel().setColor(0xffffffff);
		temposliderB.setCaptionLabel(" bpm");
		temposliderB.getCaptionLabel().toUpperCase(false);
		temposliderB.setColorValueLabel(0xffffffff);
		temposliderB.setColorBackground(0xff31332e);
		temposliderB.setSliderMode(0);
		
		/*tempodecB = controlp5.addButton("tempoincB", 0, b_infodisplayX, b_spectrumY+b_spectrumH + 20, 20, 20).setSwitch(true);
		tempodecB.captionLabel().setColor(0);
		tempodecB.setCaptionLabel("-");
		tempoincB = controlp5.addButton("tempodecB", 0, b_infodisplayX+b_infodisplayW - 20, b_spectrumY+b_spectrumH+20, 20, 20).setSwitch(true);
		tempoincB.captionLabel().setColor(0);
		tempoincB.setCaptionLabel("+");	*/	
		

		crossfadeslider = controlp5.addSlider("intCrossfade", 0, 96, 48, width/2-191, crossfaderY+3, crossfaderW-2, 38);
		crossfadeslider.getCaptionLabel().setVisible(false);
		crossfadeslider.getValueLabel().setVisible(false);
		crossfadeslider.setColorBackground(0xff31332e);
		crossfadeslider.setVisible(true);
		
		//central bar
		crossfadeslider.setSliderMode(0);
		
		plcontrolgroup = controlp5.addGroup("playlistGroup", 11, height-33);
		plcontrolgroup.disableCollapse();
		plcontrolgroup.hideBar();		
		
		// PLAYLIST BUTTONS - swap, add, remove, add-by-url, load m3u, save m3u to clipboard		
		listAadd = controlp5.addButton("listAadd", 0).setSwitch(false);
		listAadd.setGroup(plcontrolgroup);
		listAadd.setCaptionLabel("add");
		listAadd.getCaptionLabel().getStyle().marginLeft = 3;
		listAadd.getCaptionLabel().getStyle().marginTop = -1;
		listAadd.setColorCaptionLabel(1);
		listAadd.setColorBackground(color(foregroundcol));
		listAadd.setColorActive(foreshadecol);
		listAadd.setPosition(3, 0);
		listAadd.setHeight(24);		
		listAadd.setWidth(45);
		
		listAremove = controlp5.addButton("listAremove", 0).setSwitch(false);
		listAremove.setGroup(plcontrolgroup);
		listAremove.setCaptionLabel("remove");
		listAremove.getCaptionLabel().getStyle().marginLeft = 0;
		listAremove.getCaptionLabel().getStyle().marginTop = 0;
		listAremove.setColorCaptionLabel(1);
		listAremove.setColorBackground(color(foregroundcol));
		listAremove.setColorActive(foreshadecol);
		listAremove.setPosition(3 + listAadd.getWidth(),0);
		listAremove.setWidth(72);
		listAremove.setHeight(24);	
		listAremove.hide();
		
		listAloadm3u = controlp5.addButton("listAloadm3u", 0).setSwitch(false);
		listAloadm3u.setGroup(plcontrolgroup);
		listAloadm3u.setCaptionLabel("load m3u");
		listAloadm3u.getCaptionLabel().getStyle().marginLeft = 4;
		listAloadm3u.getCaptionLabel().getStyle().marginTop = -1;
		listAloadm3u.setColorCaptionLabel(1);
		listAloadm3u.setColorBackground(color(foregroundcol));
		listAloadm3u.setColorActive(foreshadecol);
		listAloadm3u.setPosition(9 + listAadd.getWidth(),0);
		listAloadm3u.setWidth(93);
		listAloadm3u.setHeight(24);
		
		listAsavem3u = controlp5.addButton("listAsavem3u", 0).setSwitch(false);
		listAsavem3u.setGroup(plcontrolgroup);
		listAsavem3u.setCaptionLabel("save m3u");
		listAsavem3u.getCaptionLabel().getStyle().marginLeft = 4;
		listAsavem3u.getCaptionLabel().getStyle().marginTop = -1;
		listAsavem3u.setColorCaptionLabel(1);
		listAsavem3u.setColorBackground(color(foregroundcol));
		listAsavem3u.setColorActive(foreshadecol);
		listAsavem3u.setPosition(18 + listAloadm3u.getWidth()+  listAadd.getWidth(), 0);
		listAsavem3u.setWidth(93);
		listAsavem3u.setHeight(24);
		// PLAYLIST BUTTONS - A - END

		plcontrolgroupB = controlp5.addGroup("playlistGroupB", 865, height-33);
		plcontrolgroupB.disableCollapse();
		plcontrolgroupB.hideBar();		
		
		// PLAYLIST BUTTONS - swap, add, remove, add-by-url, load m3u, save m3u to clipboard		
		listBadd = controlp5.addButton("listBadd", 0).setSwitch(false);
		listBadd.setGroup(plcontrolgroupB);
		listBadd.setCaptionLabel("add");
		listBadd.getCaptionLabel().getStyle().marginLeft = 3;
		listBadd.getCaptionLabel().getStyle().marginTop = -1;
		listBadd.setColorCaptionLabel(1);
		listBadd.setColorBackground(color(foregroundcol));
		listBadd.setColorActive(foreshadecol);
		listBadd.setPosition(3, 0);
		listBadd.setHeight(24);		
		listBadd.setWidth(45);
		
		listBremove = controlp5.addButton("listBremove", 0).setSwitch(false);
		listBremove.setGroup(plcontrolgroupB);
		listBremove.setCaptionLabel("remove");
		listBremove.getCaptionLabel().getStyle().marginLeft = -2;
		listBremove.getCaptionLabel().getStyle().marginTop = -2;
		listBremove.setColorCaptionLabel(1);
		listBremove.setColorBackground(color(foregroundcol));
		listBremove.setColorActive(foreshadecol);
		listBremove.setPosition(3 + listBadd.getWidth(),0);
		listBremove.setWidth(72);
		listBremove.setHeight(24);	
		listBremove.hide();
		
		listBloadm3u = controlp5.addButton("listBloadm3u", 0).setSwitch(false);
		listBloadm3u.setGroup(plcontrolgroupB);
		listBloadm3u.setCaptionLabel("load m3u");
		listBloadm3u.getCaptionLabel().getStyle().marginLeft = 4;
		listBloadm3u.getCaptionLabel().getStyle().marginTop = -1;
		listBloadm3u.setColorCaptionLabel(1);
		listBloadm3u.setColorBackground(color(foregroundcol));
		listBloadm3u.setColorActive(foreshadecol);
		listBloadm3u.setPosition(9 + listBadd.getWidth(),0);
		listBloadm3u.setWidth(93);
		listBloadm3u.setHeight(24);
		
		listBsavem3u = controlp5.addButton("listBsavem3u", 0).setSwitch(false);
		listBsavem3u.setGroup(plcontrolgroupB);
		listBsavem3u.setCaptionLabel("save m3u");
		listBsavem3u.getCaptionLabel().getStyle().marginLeft = 4;
		listBsavem3u.getCaptionLabel().getStyle().marginTop = -1;
		listBsavem3u.setColorCaptionLabel(1);
		listBsavem3u.setColorBackground(color(foregroundcol));
		listBsavem3u.setColorActive(foreshadecol);
		listBsavem3u.setPosition(18 + listBloadm3u.getWidth()+  listBadd.getWidth(), 0);
		listBsavem3u.setWidth(93);
		listBsavem3u.setHeight(24);
		// PLAYLIST BUTTONS - A - END	
		
		
		playlistCP5_a = controlp5.addListBox("playlist_a", infodisplayX+5, infodisplayY+10, infodisplayW-20, infodisplayH-12);
		playlistCP5_a.setColorBackground(color(0x001a1816));
		playlistCP5_a.setColorActive(foregroundcol);
		playlistCP5_a.hideBar();
		playlistCP5_a.showScrollbar();
		playlistCP5_a.disableCollapse();

		playlistCP5_b = controlp5.addListBox("playlist_b", b_infodisplayX+5, b_infodisplayY+10, b_infodisplayW-20, b_infodisplayH-12);
		playlistCP5_b.setColorBackground(color(0x001a1816));
		playlistCP5_b.setColorActive(foregroundcol);
		playlistCP5_b.hideBar();
		playlistCP5_b.disableCollapse();
		
		midi2 = controlp5.addListBox("midi2", 6 , 27, 300, 750);
		midi2.setMoveable(false);
		midi2.actAsPulldownMenu(true);
		midi2.setItemHeight(30);
		midi2.setBarHeight(30);
		midi2.getCaptionLabel().toUpperCase(true);
		midi2.getCaptionLabel().set("MIDI Device 1");
		midi2.getCaptionLabel().getStyle().marginTop = 5;
		midi2.getValueLabel().getStyle().marginTop = 5;
		midi2.setColorBackground(color(0,200));
		midi2.setColorLabel(color(255,255));		
		midi2.setColorValue(color(0,222));
		midi2.setColorActive(foregroundcol);
		String[][] list_of_devices = MidiBus.returnList();
		for (int i=0;i<list_of_devices.length;i++){
			midi2.addItem(i + ":" + list_of_devices[i][0] + " " + list_of_devices[i][1], i);
		}
		
		midi4 = controlp5.addListBox("midi4", 9 + midi2.getWidth() , 27, 300, 750);
		midi4.setMoveable(false);
		midi4.actAsPulldownMenu(true);
		midi4.setItemHeight(30);
		midi4.setBarHeight(30);
		midi4.getCaptionLabel().toUpperCase(true);
		midi4.getCaptionLabel().set("MIDI Device 2");
		midi4.getCaptionLabel().getStyle().marginTop = 5;
		midi4.getValueLabel().getStyle().marginTop = 5;
		midi4.setColorBackground(color(0,200));
		midi4.setColorLabel(color(255,255));		
		midi4.setColorValue(color(0,222));
		midi4.setColorActive(foregroundcol);
		String[][] list_of_devices2 = MidiBus.returnList();
		for (int i=0;i<list_of_devices.length;i++){
			midi4.addItem(i + ":" + list_of_devices2[i][0] + " " + list_of_devices2[i][1], i);
		}

		midi3 = controlp5.addListBox("midi3", 12  + midi2.getWidth() + midi4.getWidth() , 27, 300, 750);
		midi3.setMoveable(false);
		midi3.actAsPulldownMenu(true);
		midi3.setItemHeight(30);
		midi3.setBarHeight(30);
		midi3.getCaptionLabel().toUpperCase(true);
		midi3.getCaptionLabel().set("MIDI Note Device");
		midi3.getCaptionLabel().getStyle().marginTop = 5;
		midi3.getValueLabel().getStyle().marginTop = 5;
		midi3.setColorBackground(color(0,200));
		midi3.setColorLabel(color(255,255));		
		midi3.setColorValue(color(0,222));
		midi3.setColorActive(foregroundcol);
		String[][] list_of_inputs = MidiBus.returnList();
		for (int i=0;i<list_of_inputs.length;i++){
			midi3.addItem(i + ":" + list_of_inputs[i][0] + " " + list_of_devices[i][1], i);
		}

		crossfaderbehaviour = controlp5.addListBox("crossfadebehaviour", 15 + midi2.getWidth() + midi3.getWidth() + midi4.getWidth(), 27, 108, 320);
		crossfaderbehaviour.getCaptionLabel().setColor(0);
		crossfaderbehaviour.setMoveable(false);
		crossfaderbehaviour.actAsPulldownMenu(true);
		crossfaderbehaviour.setItemHeight(30);
		crossfaderbehaviour.setBarHeight(30);
		crossfaderbehaviour.getCaptionLabel().toUpperCase(true);
		crossfaderbehaviour.getCaptionLabel().set("X-CURVE");
		crossfaderbehaviour.getCaptionLabel().getStyle().marginTop = 5;
		crossfaderbehaviour.getValueLabel().getStyle().marginTop = 5;
		crossfaderbehaviour.setColorBackground(color(0,200));
		crossfaderbehaviour.setColorLabel(color(255,255));		
		crossfaderbehaviour.setColorValue(color(0,222));
		crossfaderbehaviour.setColorActive(foregroundcol);
		crossfaderbehaviour.addItem("BEATMATCH", 0);
		//crossfaderbehaviour.addItem("BEATMATCH", 1);
		crossfaderbehaviour.addItem("A-BIAS CUT", 2);
		crossfaderbehaviour.addItem("B-BIAS CUT", 3);
		crossfaderbehaviour.addItem("FADE", 4);
		
		slicebehaviour = controlp5.addListBox("slicebehaviour", 18 + midi2.getWidth() + midi3.getWidth() + midi4.getWidth() +  crossfaderbehaviour.getWidth(), 27, 108, 120);
		slicebehaviour.getCaptionLabel().setColor(0);
		slicebehaviour.actAsPulldownMenu(true);
		slicebehaviour.setItemHeight(30);
		slicebehaviour.setBarHeight(30);
		slicebehaviour.getCaptionLabel().toUpperCase(true);
		slicebehaviour.getCaptionLabel().set("SLICES");
		slicebehaviour.getCaptionLabel().getStyle().marginTop = 5;
		slicebehaviour.getValueLabel().getStyle().marginTop = 5;
		slicebehaviour.setColorBackground(color(0,200));
		slicebehaviour.setColorLabel(color(255,255));		
		slicebehaviour.setColorValue(color(0,222));
		slicebehaviour.setColorActive(foregroundcol);
		slicebehaviour.addItem("Row 0", 1);
		slicebehaviour.addItem("Continue", 0);


		
		//pre-load some colours
		midi2.setColorForeground(foregroundcol);
		midi4.setColorForeground(foregroundcol);
		midi3.setColorForeground(foregroundcol);
		crossfaderbehaviour.setColorForeground(foregroundcol);		
		playlistCP5_a.setColorForeground(foregroundcol);
		playlistCP5_b.setColorForeground(foregroundcol);
		crossfaderbehaviour.setColorActive(foregroundcol);
		midi2.setColorActive(foregroundcol);
		midi3.setColorActive(foregroundcol);		
		//joysticklist.setColorForeground(foregroundcol);
		//joysticklist.setColorActive(foregroundcol);
		

		
		// Load predefined playlist directories as specified in chipdisco.properties
		// This is NOT foolproof - it seems that some weirdly formatted/malformed MODs can pass the header check,
		//
		
		File deckAdirectory = new File(deckAdir);
		File deckBdirectory = new File(deckBdir);
		File[] listOfFiles = deckAdirectory.listFiles();
		File[] listOfFilesB = deckBdirectory.listFiles();
		if (listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				String temppath = StringEscapeUtils.escapeJava(listOfFiles[i]
						.getPath());
				String tempname = temppath.substring(temppath.lastIndexOf('/'))
						.substring(1);
				if (deckA.headerCheck(temppath)) {
					// I need to escape these - some files are going missing and leaving the List out of sync with the labels
					playlistCP5_a.addItem(temppath, playlistCP5_a.getListBoxItems().length);
					// Stripping the full path, leaving just the filename for display
					playlistCP5_a.getItem(temppath).setText(tempname);
				} else {
					println("Module " + tempname + " failed headerCheck");
				}

			}
		} else { //load BLANK.mod
			playlistCP5_a.addItem("BLANK.mod", 0);
		}
		if (listOfFilesB != null) {
			for (int i = 0; i < listOfFilesB.length; i++) {
				String temppath = StringEscapeUtils.escapeJava(listOfFilesB[i]
						.getPath());
				String tempname = temppath.substring(temppath.lastIndexOf('/'))
						.substring(1);
				if (deckB.headerCheck(temppath)) {
					playlistCP5_b.addItem(temppath, playlistCP5_b.getListBoxItems().length);
					playlistCP5_b.getItem(temppath).setText(tempname);
				}
			}
		} else { //load BLANK.mod
			playlistCP5_b.addItem("BLANK.mod", 0);
		}
		
		drop = new SDrop(this);
		droplistenerA = new ChipdiscoDropListenerA();
		drop.addDropListener(droplistenerA);
		droplistenerB = new ChipdiscoDropListenerB();
		drop.addDropListener(droplistenerB);		
		
		//Deck A initial play from list
		
		if(playlistCP5_a.getListBoxItems().length > 0) {
			//deckA.doModLoad(playlistCP5_a.getItem(0).getName(), false, 64);
			modLoader(playlistCP5_a.getItem(0).getName(), false, 48, 0, playlistCP5_a.getItem(0).getId());
			liveinstrument[0] = -1;
			deckA.setOverridetempo(false);
			//temposliderA.setValue(deckA.initialtempo);
			translideA.setValue(12);
			cuedeck('a', true);
		}
		
		if(playlistCP5_b.getListBoxItems().length > 0) {
			//deckB.doModLoad(playlistCP5_b.getItem(0).getName(), false, 64);
			modLoader(playlistCP5_b.getItem(0).getName(), false, 48, 1, playlistCP5_b.getItem(0).getId());
			liveinstrument[0] = -1;
			deckB.setOverridetempo(false);
			temposliderB.setValue(deckB.initialtempo);
			translideB.setValue(12);
			cuedeck('b', true);
		}
		

		try {
			if(foundprops) {
				myBus = new MidiBus(this, Integer.parseInt(props.getProperty("midiInput1")), 8);
			} else {
				myBus = new MidiBus(this, midiIndevice, 8);
			}
			MidiBus.list();
			ControlBusname = myBus.getBusName();
			noteInputbus = new MidiBus(this, midiNotedevice, 8);
			NoteBusname = noteInputbus.getBusName();		
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		//Let's get the image buttons up and running...

		// A		(filename, xpos, ypos, buttonID)
		decbpmbt = new ControlButton("controls_11BIG.png", 11, 233, 2);
		bpmgfxA = new ControlButton("bpmBIG.png", 77, 239, 19);
		incbpmbt = new ControlButton("controls_07BIG.png", 303, 233, 3);		
		syncbt = new ControlButton("controls_12BIG.png", 363, 233, 1);		
		playbt = new ControlButton("controls_03BIG.png", 441, 233, 4);
		cuebt = new ControlButton("controls_05BIG.png", 501, 233, 7);
		loopbt = new ControlButton("controls_14BIG.png", 561, 233, 8);		
		emptybt = new ControlButton("controls_15BIG.png", 255, 75, 9);
		
		// B
				
		loopbtB = new ControlButton("controls_14BIG.png", 668, 233, 14);
		cuebtB = new ControlButton("controls_05BIG.png", 728, 233, 13);		
		playbtB = new ControlButton("controls_03BIG.png", 783, 233, 10);		
		syncbtB = new ControlButton("controls_12BIG.png", 866, 233, 17);		
		decbpmbtB = new ControlButton("controls_11BIG.png", 926, 233, 15);
		bpmgfxB = new ControlButton("bpmBIG.png", 996, 239, 20);
		incbpmbtB = new ControlButton("controls_07BIG.png", 1218, 233, 16);
		emptybtB = new ControlButton("controls_15BIG.png", 15, 233, 18);
		
		crossfadergfx = new ControlButton("crossfadeBIG.png", width/2-194, 300, 21);
		crossfadergfx.btW = 389;
		crossfadergfx.btH = 44;
		bpmgfxA.btW = 216;
		bpmgfxB.btW = 215;
		bpmgfxA.btH = 44;
		bpmgfxB.btH = 44;
		volgfxA = new ControlButton("volBIG.png", width/2 - 75, 42, 22);
		volgfxB = new ControlButton("volBIG.png", width/2 + 32, 42, 22);
		volgfxA.btW = 44;
		volgfxB.btW = 44;
		volgfxA.btH = 180;
		volgfxB.btH = 180;
		transpgfx = new ControlButton("transposeBIG.png", width/2 - 27, 87, 11);
		transpgfxB = new ControlButton("transposeBIG.png", width/2+3, 87, 11);
		transpgfx.btW = 23;
		transpgfxB.btW = 23;
		transpgfx.btH = 90;
		transpgfxB.btH = 90;
		
		infodisplaygfx = new ControlButton("infodisplayBIG.png", 14, 298, 22);
		infodisplaygfxB = new ControlButton("infodisplayBIG.png", width - 12 - 399, 298, 22);
		infodisplaygfx.btH = 292;
		infodisplaygfx.btW = 399;
		infodisplaygfxB.btH = 292;
		infodisplaygfxB.btW = 399;
	
		updateControlcolours();
		
	}
	
	public static void main(String args[]) {
		//PApplet.main(new String[] { "--present", "Chipdisco" });
		PApplet.main(new String[] {"Chipdisco6" });
	}

	
	public void draw() {
		if(millis() - doubleclick > 200f) {
			doubleclick = 0f;	
		}
		if(millis() - doublerightclick > 200f) {
			doublerightclick = 0f;
		}

		fill(49,51,46);
		rect(0,0,width,height);		
		noFill();
		line(0,0,0,height);
		noStroke();		

		infodisplaygfx.display(); infodisplaygfxB.display();					
		displayInfo(0, 'a'); displayInfo(0, 'b');
		
		textFont(syntaxterror32, 32);
		//text(titlestring , width/2 - 124, height-162);
		image(cdlogo, width/2-195, height-257);
		image(credits, width/2-190, height-185);
		textFont(helvetica15, 15);
		text(aboutstring, width/2 - 201, height-192);
		fill(255,255);
		
		if(deckA.loadSuccess > 0) {
			
			if(deckA.player.ibxm.current_row == 0 && deckAslavesync) {
				deckB.setNext_row(0);
				deckAslavesync = false;
			}
			
			patternLoop();
			int deckAlenSeconds = round(deckA.songLength % 60);
			int deckAlenMinutes = round(deckA.songLength / 60);
			text("CHANS:" + deckA.numchannels + " POS:" + deckA.getCurrent_sequence_index() + "/" + deckA.numpatterns + "  INIT BPM:" + deckA.initialtempo + "  " +
					"LEN:" + deckAlenMinutes + ":" + deckAlenSeconds, playlistCP5_a.getPosition().x, 225);
		}
		if(deckB.loadSuccess > 0) {
			
			if(deckB.player.ibxm.current_row == 0 && deckBslavesync) {
				deckA.setNext_row(0);
				deckBslavesync = false;
			}
			
			patternLoopB();
			int deckBlenSeconds = round(deckB.songLength % 60);
			int deckBlenMinutes = round(deckB.songLength / 60);
			text("CHANS:" + deckB.numchannels + " POS:" + deckB.getCurrent_sequence_index() + "/" + deckB.numpatterns + "  INIT BPM:" + deckB.initialtempo + "  " +
					"LEN:" + deckBlenMinutes + ":" + deckBlenSeconds, playlistCP5_b.getPosition().x, 225);
		}
		
		
		syncbt.display(); decbpmbt.display(); incbpmbt.display();
		playbt.display();  cuebt.display(); loopbt.display();
		syncbtB.display(); decbpmbtB.display(); incbpmbtB.display();
		playbtB.display(); cuebtB.display(); loopbtB.display();
		crossfadergfx.display();
		bpmgfxA.display(); bpmgfxB.display(); volgfxA.display(); volgfxB.display();
		transpgfx.display(); transpgfxB.display();

		//end draw	
	}
	
	public void patternLoop(){
		int totalrows = deckA.player.ibxm.total_rows;
		int looplength = totalrows/loopdivision;
		if (loopcurrentsegment == true){
			loopbt.override = true;
			if (loopcaught < looplength){
				loopstart = 0;
				loopend = looplength-1;
			} else if (loopcaught > looplength && loopcaught < looplength*2){
				loopstart = looplength;
				loopend = (looplength*2)-1;
			} else if (loopcaught > looplength*2 && loopcaught < looplength*3) {
				loopstart = looplength*2;
				loopend = (looplength*3)-1;
			} else if (loopcaught > looplength*3 && loopcaught < looplength*4){
				loopstart = looplength*3;
				loopend = (looplength*4)-1;
			}

			if (deckA.getCurrent_row() == loopend){
				if (loopend == looplength*loopdivision-1){
					deckA.setNext_sequence_index(deckA.getCurrent_sequence_index(), 0);
					deckA.setNext_row(loopstart);
				} else {
					deckA.setNext_row(loopstart);
				}

			}
		}
	}

	public void patternLoopB(){
		int totalrows = deckB.player.ibxm.total_rows;
		int looplength = totalrows/b_loopdivision;
		if (loopcurrentsegmentB == true){
			loopbtB.override = true;
			if (loopcaughtB < looplength){
				loopstartB = 0;
				loopendB = looplength-1;
			} else if (loopcaughtB > looplength && loopcaughtB < looplength*2){
				loopstartB = looplength;
				loopendB = (looplength*2)-1;
			} else if (loopcaughtB > looplength*2 && loopcaughtB < looplength*3) {
				loopstartB = looplength*2;
				loopendB = (looplength*3)-1;
			} else if (loopcaughtB > looplength*3 && loopcaughtB < looplength*4){
				loopstartB = looplength*3;
				loopendB = (looplength*4)-1;
			}

			if (deckB.getCurrent_row() == loopendB){
				if (loopendB == looplength*b_loopdivision-1){
					deckB.setNext_sequence_index(deckB.getCurrent_sequence_index(), 0);
					deckB.setNext_row(loopstartB);
				} else {
					deckB.setNext_row(loopstartB);
				}

			}
		}
	}

	public void mousePressed() {
		// Experimental panning stuff! ONLY to be used for MOD; any format with its own pan commands will ignore it 
		// or be messed up. Do it all while checking the header.
		for (int i=0; i < deckA.numchannels; i++) {
			deckA.player.ibxm.channels[i].set_panning(0);
		}
		
		for (int i=0; i < deckB.numchannels; i++) {
			deckB.player.ibxm.channels[i].set_panning(255);
		}
		
		if (mouseEvent.getClickCount() == 2) {
			if(mouseButton == LEFT) {
				doubleclick = millis(); // General doubleclick timer for playlists
			} else if(mouseButton == RIGHT) {
				doublerightclick = millis();
			}						
			
		}
		
		if (!midi2.isOpen() && !midi3.isOpen() ) {
				if (mouseButton == LEFT) {

					// Mute channels from GUI blocks
					for (int c=0; c<deckA.numchannels; c++) {
						if(chanmutes[c]) {
							if(deckA.getChanmute(c)) {
								deckA.setChanmute(c, false);
							} else {
								deckA.setChanmute(c, true);
							}
						}
					}
					for (int c=0; c<deckB.numchannels; c++) {
						if(chanmutesB[c]) {
							if(deckB.getChanmute(c)) {
								deckB.setChanmute(c, false);
							} else {
								deckB.setChanmute(c, true);
							}
						}
					}
					
					
					//DECK A




					if (deckA.loadSuccess > 0) {
						//DECK A STUFF
						//if (player.running == true && currentTime > 5000) {
						if (deckA.playing == true) {
							doPosition('a');
						}
					
						// Playlist click Deck A
					}
				} //mouseButton == LEFT <- end
					
			}

					if (deckB.loadSuccess > 0) {
						//DECK B STUFF
						//if (player.running == true && currentTime > 5000) {
						if (deckB.playing == true) {
							doPosition('b');
						}
				
						//END OF DECK B STUFF
					}
					
				
				// MAIN CONTROL BUTTONS:
				// 1: syncbt, 2: decbpmbt, 3: incbpmbt, 4: playbt, 6: mutebt, 7: cuebt, 8: loopbt, 9: emptybt,
				// 10: playbtB, 12: mutebtB, 13: cuebtB, 14: loopbtB, 15: decbpmB, 16: incbpmB, 17: syncbtB, 18: emptybtB,
				if(playbt.mouseover) {
					 if(deckA.paused) {
						deckA.play();
						playbt.btimg = loadImage("controls_04BIG.png");
					} else {
						deckA.pause();
						playbt.btimg = loadImage("controls_03BIG.png");
					}
				}
				if(playbtB.mouseover) {
					 if(deckB.paused) {
						deckB.play();
						playbtB.btimg = loadImage("controls_04BIG.png");
					} else {
						deckB.pause();
						playbtB.btimg = loadImage("controls_03BIG.png");
					}
				}
				if(decbpmbt.mouseover) {
					
					if(deckA.getTempo()-1 >= 31) {
						deckA.setTempo(deckA.getTempo()-1);
						temposliderA.setValue(deckA.getTempo());						
					}
				}
				if(incbpmbt.mouseover) {
					if(deckA.getTempo()+1 <= 256) {
						deckA.setTempo(deckA.getTempo()+1);
						temposliderA.setValue(deckA.getTempo());
					}
				}
				if(decbpmbtB.mouseover) {
					if(deckB.getTempo()-1 >= 31) {
						deckB.setTempo(deckB.getTempo()-1);
						temposliderB.setValue(deckB.getTempo());
					}	
				}
				if(incbpmbtB.mouseover) {
					
					if(deckB.getTempo()+1 <= 256) {
						deckB.setTempo(deckB.getTempo()+1);
						temposliderB.setValue(deckB.getTempo());
					}
				}
				if(cuebt.mouseover) {
					if(deckAcue) {
						cuedeck('a', false);
						cuebt.override = true;
						deckAcue = false;
					} else {
						cuedeck('a', true);
						cuebt.override = false;
						deckAcue = true;
					}
				}
				if(cuebtB.mouseover) {
					if(deckBcue) {
						cuedeck('b', false);
						cuebtB.override = true;
						deckBcue = false;
					} else {
						cuedeck('b', true);
						cuebtB.override = false;
						deckBcue = true;
					}
				}
				if(loopbt.mouseover) {
					if (loopcurrentsegment == false) {
						loopcurrentsegment = true;
						loopbt.override = true;
					} else {
						loopcurrentsegment = false;
						loopbt.override = false;
					}
				}
				if(loopbtB.mouseover) {
					if (loopcurrentsegmentB == false) {
						loopcurrentsegmentB = true;
						loopbtB.override = true;
					} else {			
						loopcurrentsegmentB = false;
						loopbtB.override = false;
					}
				}
				if(syncbt.mouseover) {
					deckB.setTempo(deckA.getTempo());
					temposliderB.setValue(deckA.getTempo());
					deckAslavesync = true;
				}
				if(syncbtB.mouseover) {
					deckA.setTempo(deckB.getTempo());
					temposliderA.setValue(deckB.getTempo());
					deckBslavesync = true;
				}


				if (mouseButton == RIGHT) {


				}

			

	}
	
	public void mouseDragged() {

	}
	
	public void keyPressed() {
			//println(key);
			println(keyCode);
			//all-purpose 'display some info' key for debugging stuff
		
			if (keyCode == 16) {
				shiftpressed = true;
			}

		
			
			if (keyCode == 17) {
				ctrlpressed = true;
			}
			
			if (keyCode == 18) {
				altpressed = true;
			}
			
			if (keyCode == 192) {

			}
			
			//49-56 = 0-7 (keys 1-8)
			//BLOCK 1 of 4
				switch(keyCode) {
				case 49:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[0], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[0], slicetriggerbehaviour);}
					break;
				case 50:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[1], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[1], slicetriggerbehaviour);}
					break;
				case 51:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[2], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[2], slicetriggerbehaviour);}
					break;
				case 52:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[3], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[3], slicetriggerbehaviour);}
					break;
				case 53:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[4], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[4], slicetriggerbehaviour);}
					break;
				case 54:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[5], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[5], slicetriggerbehaviour);}
					break;
				case 55:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[6], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[6], slicetriggerbehaviour);}
					break;
				case 56:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[7], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[7], slicetriggerbehaviour);}
					break;
				}
			
				//BOCK 2 of 4
				switch(keyCode) {
				case 81:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[8], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[8], slicetriggerbehaviour);}
					break;
				case 87:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[9], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[9], slicetriggerbehaviour);}
					break;
				case 69:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[10], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[10], slicetriggerbehaviour);}
					break;
				case 82:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[11], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[11], slicetriggerbehaviour);}
					break;
				case 84:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[12], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[12], slicetriggerbehaviour);}
					break;
				case 89:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[13], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[13], slicetriggerbehaviour);}
					break;
				case 85:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[14], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[14], slicetriggerbehaviour);}
					break;
				case 73:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[15], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[15], slicetriggerbehaviour);}
					break;
				}
				
				//BOCK 3 of 4
				switch(keyCode) {
				case 65:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[16], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[16], slicetriggerbehaviour);}
					break;
				case 83:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[17], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[17], slicetriggerbehaviour);}
					break;
				case 68:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[18], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[18], slicetriggerbehaviour);}
					break;
				case 70:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[19], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[19], slicetriggerbehaviour);}
					break;
				case 71:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[20], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[20], slicetriggerbehaviour);}
					break;
				case 72:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[21], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[21], slicetriggerbehaviour);}
					break;
				case 74:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[22], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[22], slicetriggerbehaviour);}
					break;
				case 75:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[23], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[23], slicetriggerbehaviour);}
					break;
				}
				
				//BOCK 4 of 4
				switch(keyCode) {
				case 90:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[24], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[24], slicetriggerbehaviour);}
					break;
				case 88:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[25], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[25], slicetriggerbehaviour);}
					break;
				case 67:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[26], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[26], slicetriggerbehaviour);}
					break;
				case 86:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[27], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[27], slicetriggerbehaviour);}
					break;
				case 66:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[28], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[28], slicetriggerbehaviour);}
					break;
				case 78:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[29], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[29], slicetriggerbehaviour);}
					break;
				case 77:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[30], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[30], slicetriggerbehaviour);}
					break;
				case 44:
					if(shiftpressed) {deckA.setNext_sequence_index(slicetargets[31], slicetriggerbehaviour);}
					else if(altpressed) {deckB.setNext_sequence_index(slicetargetsB[31], slicetriggerbehaviour);}
					break;
				}
			
			// sync with spacebar!
			if (keyCode == 32) {
				//always row 0
				deckA.setNext_row(0);
				deckB.setNext_row(0);
			}
			//or separate row-0s with V and B
			if (key == 'v') {
				deckA.setNext_row(0);
			}
			if(key == 'b') {
				deckB.setNext_row(0);
			}
			// Move crossfader from A to B [end]
			if (keyCode == 35) {
				if (crossfade < crossfaderW-1) {
					crossfade += 2;
				}
			}
			// Move crossfader from B to A [Del]
			if (keyCode == 127) {
				if (crossfade > 1) {
					crossfade -= 2;
				}
			}
			//Deck A increase tempo
			if (key == '=') {
				if (bpmvalue < 255) {
					int tempoadjust = bpmvalue + 1;
					deckA.setTempo(tempoadjust);
					bpmvalue = tempoadjust;
				}
			}
			//Deck A decrease tempo
			if (key == '-') {
				if (bpmvalue > 32) {
					int tempoadjust = bpmvalue - 1;
					deckA.setTempo(tempoadjust);
					bpmvalue = tempoadjust;
				}
			}
			//Deck B increase tempo
			if (key == '+') {
				if (bpmvalueB < 255) {
					int tempoadjust = bpmvalueB + 1;
					deckB.setTempo(tempoadjust);
					bpmvalueB = tempoadjust;
				}
			}
			//Deck B decrease tempo
			if (key == '_') {
				if (bpmvalueB > 32) {
					int tempoadjust = bpmvalueB - 1;
					deckB.setTempo(tempoadjust);
					bpmvalueB = tempoadjust;
				}
			}
			//pause Deck A
			if (key == 'o') {
				if (paused == 1) {
					paused = 0;
					deckA.play();
					playbt.btimg = loadImage("controls_04BIG.png");
				} else {
					paused = 1;
					deckA.pause();
					playbt.btimg = loadImage("controls_03BIG.png");
				}
			}
			//pause Deck B
			if (key == 'p') {
				if (pausedB == 1) {
					pausedB = 0;
					deckB.play();
					playbtB.btimg = loadImage("controls_04BIG.png");
				} else {
					pausedB = 1;
					deckB.pause();
					playbtB.btimg = loadImage("controls_03BIG.png");
				}
			}
			// Deck A loopsegment
			if (key == 'k') {
				if (loopcurrentsegment == true) {
					loopcurrentsegment = false;
					loopbt.override = false;
				} else {
					loopcurrentsegment = true;
					loopbt.override = true;
					loopcaught = deckA.getCurrent_row();
				}
			}
			// Deck B loopsegment
			if (key == 'l') {
				if (loopcurrentsegmentB == true) {
					loopcurrentsegmentB = false;
					loopbtB.override = false;
				} else {
					loopcurrentsegmentB = true;
					loopbtB.override = true;
					loopcaughtB = deckB.getCurrent_row();
				}
			}
			
			//This does NOTHING. 
			if (key == 'z') {

			}
			// Too resource intensive, plus I want to use 'x' for something else
			/*if(key == 'x') {
				for (int i=0; i < deckA.numchannels; i++) {
					//deckA.effector(i,"e","91");
				}
				
			}*/
			
			// Toggle slice trigger behaviour 
			if (key == 'x') {
				if (slicetriggerbehaviour == 0) {
					slicetriggerbehaviour = 1;
				} else {
					slicetriggerbehaviour = 0;
				}
			}
			
			
			//Deck A muting
			if (key == 'n') {
				if (muted == true) {
					deckA.mute();
					muted = false;
					//mutebt.override = true;
				} else {
					deckA.mute();
					muted = true;
					//mutebt.override = false;
				}
			}
			//Deck B muting
			if (key == 'm') {
				if (mutedB == true) {
					deckB.mute();
					mutedB = false;
					//mutebtB.override = true;
				} else {
					deckB.mute();
					mutedB = true;
					//mutebtB.override = false;
				}
			}
			if (deckA.playing && !shiftpressed && !altpressed) {
				// Deck A channel-muting (chans 1-8)		
				if (key == '1') {
					if (!deckA.getChanmute(0)) {
						deckA.setChanmute(0, true);
					} else {
						deckA.setChanmute(0, false);
					}
				}
				if (key == '2') {
					if (!deckA.getChanmute(1)) {
						deckA.setChanmute(1, true);
					} else {
						deckA.setChanmute(1, false);
					}
				}
				if (key == '3') {
					if (!deckA.getChanmute(2)) {
						deckA.setChanmute(2, true);
					} else {
						deckA.setChanmute(2, false);
					}
				}
				if (key == '4') {
					if (!deckA.getChanmute(3)) {
						deckA.setChanmute(3, true);
					} else {
						deckA.setChanmute(3, false);
					}
				}
				if (key == '5' && deckA.numchannels > 4) {
					if (!deckA.getChanmute(4)) {
						deckA.setChanmute(4, true);
					} else {
						deckA.setChanmute(4, false);
					}
				}
				if (key == '6' && deckA.numchannels > 5) {
					if (!deckA.getChanmute(5)) {
						deckA.setChanmute(5, true);
					} else {
						deckA.setChanmute(5, false);
					}
				}
				if (key == '7' && deckA.numchannels > 6) {
					if (!deckA.getChanmute(6)) {
						deckA.setChanmute(6, true);
					} else {
						deckA.setChanmute(6, false);
					}
				}
				if (key == '8' && deckA.numchannels > 7) {
					if (!deckA.getChanmute(7)) {
						deckA.setChanmute(7, true);
					} else {
						deckA.setChanmute(7, false);
					}
				}
			}
			
			if (deckB.playing && !shiftpressed && !altpressed) {
				if (key == 'q') {
					if (!deckB.getChanmute(0)) {
						deckB.setChanmute(0, true);
					} else {
						deckB.setChanmute(0, false);
					}
				}
				if (key == 'w') {
					if (!deckB.getChanmute(1)) {
						deckB.setChanmute(1, true);
					} else {
						deckB.setChanmute(1, false);
					}
				}
				if (key == 'e') {
					if (!deckB.getChanmute(2)) {
						deckB.setChanmute(2, true);
					} else {
						deckB.setChanmute(2, false);
					}
				}
				if (key == 'r') {
					if (!deckB.getChanmute(3)) {
						deckB.setChanmute(3, true);
					} else {
						deckB.setChanmute(3, false);
					}
				}
				if (key == 't' && deckB.numchannels > 4) {
					if (!deckB.getChanmute(4)) {
						deckB.setChanmute(4, true);
					} else {
						deckB.setChanmute(4, false);
					}
				}
				if (key == 'y' && deckB.numchannels > 5) {
					if (!deckB.getChanmute(5)) {
						deckB.setChanmute(5, true);
					} else {
						deckB.setChanmute(5, false);
					}
				}
				if (key == 'u' && deckB.numchannels > 6) {
					if (!deckB.getChanmute(6)) {
						deckB.setChanmute(6, true);
					} else {
						deckB.setChanmute(6, false);
					}
				}
				if (key == 'i' && deckB.numchannels > 7) {
					if (!deckB.getChanmute(7)) {
						deckB.setChanmute(7, true);
					} else {
						deckB.setChanmute(7, false);
					}
				}
			}
			//hold!
			if (key == 'h') {
				deckA.setNext_row(deckA.getCurrent_row());

			}
			if (key == 'j') {

				deckB.setNext_row(deckB.getCurrent_row());

			}
			if (key == '-') {

			}
			if (key == '=') {

			}
			if (key == '[') {
				if(!deckAcue) {
					cuedeck('a', true);
					cuebt.override = true;
					deckAcue = true;
				} else {
					cuedeck('a', false);
					cuebt.override = false;
					deckAcue = false;
				}				
			}
			if (key == ']') {				
				if(!deckBcue) {
					cuedeck('b', true);
					cuebtB.override = true;
					deckAcue = true;
				} else {
					cuedeck('b', false);
					cuebtB.override = false;
					deckBcue = false;
				}	
			}
			
			
			if (key == CODED) {				
				// F1
				if(keyCode == 112) {
					loopdivision = 4;					
					loopcurrentsegment = true;
					loopbt.override = true;
					loopcaught = deckA.player.ibxm.current_row;					
				}
				// F2
				if(keyCode == 113) {
					loopdivision = 8;
					loopcurrentsegment = true;
					loopbt.override = true;
					loopcaught = deckA.player.ibxm.current_row;
				}
				// F3
				if(keyCode == 114) {
					loopdivision = 16;
					loopcurrentsegment = true;
					loopbt.override = true;
					loopcaught = deckA.player.ibxm.current_row;
				}
				// F4
				if(keyCode == 115) {
					loopdivision = 32;
					loopcurrentsegment = true;
					loopbt.override = true;
					loopcaught = deckA.player.ibxm.current_row;
				}
				
				// F5
				if(keyCode == 116) {
					b_loopdivision = 4;					
					loopcurrentsegmentB = true;
					loopbtB.override = true;
					loopcaughtB = deckB.player.ibxm.current_row;					
				}
				// F6
				if(keyCode == 117) {
					b_loopdivision = 8;
					loopcurrentsegmentB = true;
					loopbtB.override = true;
					loopcaughtB = deckB.player.ibxm.current_row;
				}
				// F7
				if(keyCode == 118) {
					b_loopdivision = 16;
					loopcurrentsegmentB = true;
					loopbtB.override = true;
					loopcaughtB = deckB.player.ibxm.current_row;
				}
				// F8
				if(keyCode == 119) {
					b_loopdivision = 32;
					loopcurrentsegmentB = true;
					loopbtB.override = true;
					loopcaughtB = deckB.player.ibxm.current_row;
				}
				
				
				if (keyCode == DOWN) {

					
				}
				if (keyCode == UP) {

				}

				if (keyCode == RIGHT) {													
					
				}
					
			}
				if (keyCode == LEFT) {

				}
						

				if (keyCode == ESC) {
					keyCode = 0;		
					if (midi2.isOpen()) {
						midi2.close();
					}
					if (midi3.isOpen()) {
						midi3.close();
					}					
					if (slicebehaviour.isOpen()) {
						slicebehaviour.close();
					}
					if(crossfaderbehaviour.isOpen()) {
						crossfaderbehaviour.close();
					}
					if(midi4.isOpen()){
						midi4.close();
					}
				}

			

			if (key == ESC) {
				key = 0;				
				if (midi2.isOpen()) {
					midi2.close();
				}
				if (midi3.isOpen()) {
					midi3.close();
				}			
				if (slicebehaviour.isOpen()) {
					slicebehaviour.close();
				}
				if(crossfaderbehaviour.isOpen()) {
					crossfaderbehaviour.close();
				}
				if(midi4.isOpen()){
					midi4.close();
				}
			}


	}

	public void keyReleased() {
		/*if (keyCode == 192) {
			println("keyup");
			if (player.ibxm.keydown) {
				player.ibxm.keydown = false;
				println("keydown false");
			}
		}*/
		
		if(keyCode >= 112 && keyCode <= 119) {
				loopcurrentsegment = false;
				loopbt.override = false;
				loopcurrentsegmentB = false;
				loopbtB.override = false;
		}
		
		if(key == CODED){
			if(keyCode == 16){
				shiftpressed = false;
			}
		}
		if(key == CODED){
			if(keyCode == 17){
				ctrlpressed = false;
			}
			
		}
		if(key == CODED) {
			if(keyCode == 18) {
				altpressed = false;
			}
		}
		
	}

	public String[] displayInfo(int debug, char deck) {
		if (deck == 'a') {
			if (deckA.loadSuccess > 0) {
				fill(0xff2B2D28, 2);
				stroke(0);
				
				if (!cpuSaver) {
					for (int c = 0; c < deckA.numchannels; c++) {
						// Channel muting. 
						if (mouseX > (infodisplayX + c * infodisplayW
								/ deckA.numchannels)
								&& mouseX < (infodisplayX + c * infodisplayW
										/ deckA.numchannels + (infodisplayW / deckA.numchannels))
								&& mouseY > height - 59 - 419
								&& mouseY < (height - 59 - 419 + 23)) {
							chanmutes[c] = true;
						} else {
							chanmutes[c] = false;
						}
						if (!deckA.getChanmute(c)) {
							fill(
									blockpalette[c]
									//,(int) map(deckA.player.ibxm.channels[c].current_note[0],0, 97, 100, 255)
											);
						} else {
							fill(0, 0);
						}
						rect(infodisplayX + c * infodisplayW
								/ deckA.numchannels, height - 59 - 419,
								infodisplayW / deckA.numchannels - 2, 23);

						// Draw bars

						if (channelbars) {
							rect(infodisplayX + c * infodisplayW
									/ deckA.numchannels, height - 74 - 419,
									infodisplayW / deckA.numchannels - 3,
									//-(int)map(deckA.player.ibxm.channels[c].chanvolfinal, 0, 64, 0, infodisplayH));
									(int) map(deckA.player.ibxm.channels[c]
											.calculate_fakevol(), 0, 12288, 0,
											-84));
						}
					}
				}
				//progress bar
				fill(blockpalette[1], 30);
				rect(infodisplayX, height-24 - 419, infodisplayW, 15);
				fill(blockpalette[1]); //blue
				rect(infodisplayX, height-24 - 419, infodisplayW/deckA.numpatterns * deckA.getCurrent_sequence_index(), 15);
				
				String[] infotext = new String[deckA.numinstruments - 1];				
				for (int i = 0; i < (deckA.numinstruments - 1); i++) {
					if (deckA.infotext[i] != null) {
						if (debug == 1) {
							println(deckA.infotext[i]);
						}
						infotext[i] = deckA.infotext[i];						
					}
				}																				
				fill(foregroundcol);
				return infotext;
			} else {
				String[] failure = { "No module loaded"};
				return failure;
			}
		} else {
			if (deckB.loadSuccess > 0) {
				fill(0xff2B2D28);
				stroke(0);

				if (!cpuSaver) {
					for (int c = 0; c < deckB.numchannels; c++) {
						if (mouseX > (b_infodisplayX + c * b_infodisplayW
								/ deckB.numchannels)
								&& mouseX < (b_infodisplayX + c
										* b_infodisplayW / deckB.numchannels + (b_infodisplayW / deckB.numchannels))
								&& mouseY > height - 59 - 419
								&& mouseY < (height - 59 - 419 + 23)) {
							chanmutesB[c] = true;
						} else {
							chanmutesB[c] = false;
						}
						if (!deckB.getChanmute(c)) {
							fill(
									blockpalette[c]
									//,(int) map(deckB.player.ibxm.channels[c].current_note[0],0, 97, 100, 255)
									);
						} else {
							fill(0, 0);
						}
						rect(b_infodisplayX + c * b_infodisplayW
								/ deckB.numchannels, height - 59 - 419,
								b_infodisplayW / deckB.numchannels - 2, 23);

						// Draw bars					
						if (channelbarsB) {
							rect(b_infodisplayX + c * b_infodisplayW
									/ deckB.numchannels, height - 74 - 419,
									b_infodisplayW / deckB.numchannels - 3,
									//-(int)map(deckA.player.ibxm.channels[c].chanvolfinal, 0, 64, 0, infodisplayH));
									(int) map(deckB.player.ibxm.channels[c]
											.calculate_fakevol(), 0, 12288, 0,
											-84));
						}
					}
				}
				//progress bar
				fill(blockpalette[1], 30);
				rect(b_infodisplayX, height-24 - 419, b_infodisplayW, 15);
				fill(blockpalette[1]); //blue
				rect(b_infodisplayX, height-24 - 419, b_infodisplayW/deckB.numpatterns * deckB.getCurrent_sequence_index(), 15);
				
				String[] infotext = new String[deckB.numinstruments - 1];
				
				for (int i = 0; i < (deckB.numinstruments - 1); i++) {
					if (deckB.infotext[i] != null) {
						if (debug == 1) {
							println(deckB.infotext[i]);
						}
						infotext[i] = deckB.infotext[i];						
					}
				}	
				
				String[] infotextB = new String[deckB.numinstruments - 1];
				for (int i = 0; i < (deckB.numinstruments - 1); i++) {
					if (deckB.infotext[i] != null) {
						if (debug == 1) {
							println(deckB.infotext[i]);
						}
						infotextB[i] = deckB.infotext[i];
					}
				}
				fill(foregroundcol);
				//popMatrix();
				return infotextB;
			} else {
				String[] failure = { "No module loaded"};
				return failure;
			}
		}
	}

	//redundant, since Processing won't let you choose a target directory/file
	public void savePlaylist(char deck) {
		// TO DO
		String[] listOut = new String[playlistCP5_a.getListBoxItems().length];		
		if (deck == 'a') {

		}
		

		
		saveStrings("playlist.m3u", listOut);

	}
	

	
	
	// REPLACE THIS WITH A LOCAL-MACHINE FILE OUTPUT
	
	public void savePlaylistToFile(char deck) {
		selectOutput("Choose a name and location for the M3U playlist " + deck, "fileSelected(deck)");				
		
	}
	
	public void fileSelected(File savePath, String deck) {
		if(savePath == null) {
			println("No output file selected...");
		} else {
			if (deck.charAt(deck.length()) == 'a') {
				String[] linesoutA = new String[playlistCP5_a.getListBoxItems().length];
				for (int i=0 ; i < linesoutA.length; i++) {
					linesoutA[i] = playlistCP5_a.getItem(i).getName();
				}
				saveStrings(savePath+"_deckA.m3u", linesoutA);
			}
			if (deck.charAt(deck.length()) == 'b') {
				String[] linesoutB = new String[playlistCP5_b.getListBoxItems().length];
				for (int i=0 ; i < linesoutB.length; i++) {
					linesoutB[i] = playlistCP5_b.getItem(i).getName();
				}
				saveStrings(savePath+"_deckB.m3u", linesoutB);
			}
		}
	}
	
	
		
	
	public class ControlButton {
		int btW = 56;
		int btH = 56;
		int btX = 0;
		int btY = 0;
		boolean mouseover;
		boolean override;
		boolean active;
		PImage btimg;
		int id;
		
		ControlButton(String imgpath, int xpos, int ypos, int buttonID) {
			btimg = loadImage(imgpath);
			btX = xpos;
			btY = ypos;
		}
		void display() {
			if(mouseX > btX && mouseX < btX+btW && mouseY > btY && mouseY < btY+btH) {
				mouseover = true;
			} else {
				mouseover = false;
			}
			if(override || mouseover) {
				image(btimg, btX, btY);
			} else {
				tint(255,180);
				image(btimg, btX, btY);
				noTint();
			}			
		}
	}
	
	public void buttonChecker(int id) {
		// 1: syncbt, 2: decbpmbt, 3: incbpmbt, 4: playbt, 5: pausebt, 6: mutebt, 7: cuebt, 8: loopbt, 9: emptybt,
		// 10: playbtB, 11: pausebtB, 12: mutebtB, 13: cuebtB, 14: loopbtB, 15: decbpmB, 16: incbpmB, 17: syncbtB, 18: emptybtB,	
	}
	
	
	void doPosition(char deck) {		
		
		if (deck == 'a') {
			if ((mouseX > 18 && mouseX < 413)
					&& (mouseY < 209 && mouseY > 191)
					&& (deckA.playing == true)) {
				deckA.setNext_sequence_index(round(map(mouseX, 20, 411, 0, deckA.numpatterns)), 0);			
			}
		}
		if (deck == 'b') {
			if ((mouseX > 875 && mouseX < 1268)
					&& (mouseY < 209 && mouseY > 191)
					&& (deckB.playing == true)) {
				deckB.setNext_sequence_index(round(map(mouseX, 875, 1268, 0, deckB.numpatterns)), 0);
			}
		}
		cueFix();
	}


	/*
	public void dropEvent(DropEvent theEvent) {
		println("FILE WAS DROPPED");
		//pushMatrix();
		//translate(0, 0);
		//translate(0, 300);
		if(theEvent.isFile() && theEvent.file().exists()) {
			println("file seems to exist");
			println("X: " + playlistCP5_a.getPosition().x + " Y: " + playlistCP5_a.getPosition().y);
			println("Width: " + playlistCP5_a.getWidth() + " Height: " + ((int)playlistCP5_a.getPosition().y + 195));
			println("MouseX: " + mouseX + "    MouseY: " + mouseY);
			//FIX THIS! Use dimensiosn for playlists
			//if (mouseX > playlistCP5_a.getPosition().x && mouseX < playlistCP5_a.getPosition().x + playlistCP5_a.getWidth() && mouseY > playlistCP5_a.getPosition().y && mouseY < ((int)playlistCP5_a.getPosition().y + 195.0)) {
			if(mouseX < width/2) {
				println("File was dropped in playlist A: " + theEvent.filePath());
				String ext = theEvent.filePath().substring(theEvent.filePath().length()-3, theEvent.filePath().length());
				if (ext.equalsIgnoreCase("m3u")){
					loadm3uPlaylist('a', theEvent.filePath());
				} else {
					if(deckA.headerCheck(theEvent.filePath())) {
						String tempname = theEvent.filePath().substring(theEvent.filePath().lastIndexOf('/')).substring(1);
						playlistCP5_a.addItem(theEvent.filePath(), playlistCP5_a.getListBoxItems().length);
						playlistCP5_a.getItem(theEvent.filePath()).setText(tempname);
					}
				}					

			} 

			//if (mouseX > playlistCP5_b.getPosition().x && mouseX < playlistCP5_b.getPosition().x + playlistCP5_b.getWidth() && mouseY > playlistCP5_b.getPosition().y && mouseY < ((int)playlistCP5_b.getPosition().y + 195.0)) {
			if(mouseX > width/2 && mouseX < width) {	
				println("File was dropped in playlist B");
				String ext2 = theEvent.filePath().substring(theEvent.filePath().length()-3, theEvent.filePath().length());
				if (ext2.equalsIgnoreCase("m3u")){
					loadm3uPlaylist('b', theEvent.filePath());
				} else {
					if(deckB.headerCheck(theEvent.filePath())) {
						String tempname = theEvent.filePath().substring(theEvent.filePath().lastIndexOf('/')).substring(1);
						playlistCP5_b.addItem(theEvent.filePath(), playlistCP5_b.getListBoxItems().length);
						playlistCP5_b.getItem(theEvent.filePath()).setText(tempname);
					}
				}					

			}				

		}
		//popMatrix();
	}
	*/
	
	
	public void cuedeck(char deck, boolean activate) {
		// MONO LEFT output is for FRONT OF HOUSE. MONO RIGHT output is for HEADPHONES.
		// This means that CUE MIX ACTIVE = HEADPHONES = 255 PAN! 
		// CUE MIX ACTIVE also means speaker icon should NOT be illuminated 
		if (deck == 'a') {					
			if(activate) {
				for (int i=0; i < deckA.numchannels; i++) {
					deckA.mono = 2;
					deckA.player.ibxm.channels[i].set_panning(255);
					
				}				
				deckAcue = true;		
				doVolume();
			} else {
				for (int i=0; i < deckA.numchannels; i++) {
					deckA.mono = 1;
					deckA.player.ibxm.channels[i].set_panning(0);
				}				
				deckAcue = false;
				doVolume();
			}

		}
		if (deck == 'b') {
			if(activate) {
				for (int i=0; i < deckB.numchannels; i++) {
					deckB.mono = 2;
					deckB.player.ibxm.channels[i].set_panning(255);
				}				
				deckBcue = true;
				doVolume();
			} else {					
				for (int i=0; i < deckB.numchannels; i++) {
					deckB.mono = 1;
					deckB.player.ibxm.channels[i].set_panning(0);
				}
				deckBcue = false;
				doVolume();				
			}
		}

	}
	
	public void controllerChange(int channel, int number, int value) {

		print("\nCC: "+number + "   Value: " +value + "  ");
		
		if (number == ccCrossfader) {
			crossfade = (int) map(value, 0, 127, 0, 96);
			crossfadeslider.setValue(crossfade);
			doVolume();
			println("[Crossfader]");
		}
		
		if( number == ccCuemixvol) {
			cuevolumeslider.setValue((int)map(value, 0, 127, 0, 48));
			println("[Cue-Mix Volume]");
		}
		
		//DECK A CUE TOGGLE
		if ( number == ccCuemixA && !altbutton) {
			if(!deckAcue) {
				cuedeck('a', true);
				cuebt.override = false;
				deckAcue = true;

			} else {
				cuedeck('a', false);
				cuebt.override = true;
				deckAcue = false;
			}
			println("[Deck A Cue-Mix Toggle]");
		}
		//DECK B CUE TOGGLE
		if ( number == ccCuemixB && !altbutton) {
			if(!deckBcue) {
				cuedeck('b', true);
				cuebtB.override = false;
				deckBcue = true;

			} else {
				cuedeck('b', false);
				cuebtB.override = true;
				deckBcue = false;

			}
			println("[Deck B Cue-Mix Toggle]");
		}
		
		// ALT KEY 
		if ( number == ccAlt) {
			if (value == 127) {
				altbutton = true;
				println("[Alt Button Pressed]");
			} 
			if (value == 0) {
				altbutton = false;
				println("[Alt Button Released]");
			}
		}
		
		// DECK A PAUSE
		if ( number == ccPauseA && !altbutton) {
			if(value == 127) {
				if (paused == 1) {
					paused = 0;
					deckA.play();
					playbt.btimg = loadImage("controls_04BIG.png");
					println("[Deck A Play]");
				} else {
					paused = 1;
					deckA.pause();
					playbt.btimg = loadImage("controls_03BIG.png");
					println("[Deck A Pause]");
				}
			}
			
		}
		
		// DECK B PAUSE
		if ( number == ccPauseB && !altbutton)  {
			if(value == 127) {
			if (paused == 1) {
				paused = 0;
				deckB.play();
				playbtB.btimg = loadImage("controls_04BIG.png");
				println("[Deck B Play]");
			} else {
				paused = 1;
				deckB.pause();
				playbtB.btimg = loadImage("controls_03BIG.png");
				println("[Deck B Pause]");
			}
			}
		
		}
		
		//DECK A BPM NUDGE
		// UP
		if ( number == ccSyncA && altbutton) {
			if (value == 127) {
				if(deckA.getTempo()+1 <= 255) {
					deckA.setTempo(deckA.getTempo()+1);
					temposliderA.setValue(deckA.getTempo());
				}
			}
			println("[Deck A Tempo Nudge Up]");
		}
		// DOWN
		if ( number == ccPauseA && altbutton) {
			if (value == 127) {
				if(deckA.getTempo()-1 >= 32) {
					deckA.setTempo(deckA.getTempo()-1);
					temposliderA.setValue(deckA.getTempo());
				}
				println("[Deck A Tempo Nudge Down]");
			}
		}
		
		//DECK B BPM NUDGE
		// UP
		if ( number == ccSyncB && altbutton) {
			if (value == 127) {
				if(deckB.getTempo()+1 <= 256) {
					deckB.setTempo(deckB.getTempo()+1);
					temposliderB.setValue(deckB.getTempo());
				}
			}
			println("[Deck B Tempo Nudge Up]");
		}
		// DOWN
		if ( number == ccPauseB && altbutton) {
			if (value == 127) {
				if(deckB.getTempo()-1 >= 31) {
					deckB.setTempo(deckB.getTempo()-1);
					temposliderB.setValue(deckB.getTempo());
				}
			}
			println("[Deck B Tempo Nudge Down]");
		}
		
		// MATCH TEMPO AND SLAVE-SYNC DECK B TO NEXT DECK A ROW0 
		if( number == ccSyncA && !altbutton && value == 127) {
			deckB.setTempo(deckA.getTempo());	
			temposliderB.setValue(deckA.getTempo());
			deckAslavesync = true;
			println("[Sync-Lock Deck B (Slave) to Deck A (Master)]");
		}
		// MATCH TEMPO AND SLAVE-SYNC DECK A TO NEXT DECK B ROW0
		if( number == ccSyncB && !altbutton && value == 127) {
			deckA.setTempo(deckB.getTempo());
			temposliderA.setValue(deckB.getTempo());
			deckBslavesync = true;
			println("[Sync-Lock Deck A (Slave) to Deck B (Master)]");
		}
		
		//SYNC DECKS
		if ( number == ccForcesync) {
			if(value == 127) {
				deckA.setNext_row(0);
				deckB.setNext_row(0);
			}
			println("[Force-Sync Decks To Row 0]");
		}
				
		
		// Deck A TEMPO ADJUST
		if ( number == ccTempoA && value == constrain((int)map(bpmvalue, 32, 255, 0, 127), 0, 127) && altbutton == false) {
			tempoknoblock = false;
		}
		if ( number == ccTempoA && altbutton == false){
			if (tempoknoblock == false){
				int tempoadjust = constrain((int)map(value, 0, 127, 32, 255), 32, 255);
				deckA.setTempo(tempoadjust);
				bpmvalue = tempoadjust;
				temposliderA.setValue(tempoadjust);
				println("[Deck A Tempo]");
			}
		}
		
		//DECK A VOLUME FADER
		if ( number == ccVolA) {			
			volumesliderA.setValue((int)map(value, 0, 127, 0, 48));
			doVolume();
			println("[Deck A Volume]");
		}		
		
		//DECK A SCAN/SEEK PATTERN
		if ( number == ccSeekA) {
			if (value == 127){
				deckA.setNext_sequence_index(deckA.getCurrent_sequence_index()+1, 0);
			}
			if (value == 0 && (deckA.getNext_row() - 4 > 0)) {
				deckA.setNext_sequence_index(deckA.getCurrent_sequence_index()-1, 0);
			}
			println("[Deck A Seek Pattern]");
		}
		
		// Deck B TEMPO ADJUST
		if ( number == ccTempoB && value == constrain((int)map(bpmvalueB, 32, 255, 0, 127), 0, 127) && altbutton == false) {
			tempoknoblockB = false;
		}
		if ( number == ccTempoB && altbutton == false){
			if (tempoknoblockB == false){
				int tempoadjustB = constrain((int)map(value, 0, 127, 32, 255), 32, 255);
				deckB.setTempo(tempoadjustB);
				temposliderB.setValue(tempoadjustB);
				//bpmvalueB = deckB.initialtempo; // Should this be here? I don't think so...
				println("[Deck B Tempo]");
			}
		}
		
		//DECK B VOLUME FADER 
		if ( number == ccVolB) {
			volumesliderB.setValue((int)map(value, 0, 127, 0, 48));
			doVolume();
			println("[Deck B Volume]");
		}

		//DECK B SCAN/SEEK PATTERN
		if ( number == ccSeekB) {
			if (value == 127){
				deckB.setNext_sequence_index(deckB.getCurrent_sequence_index()+1, 0);
			}
			if (value == 0 && (deckA.getNext_row() - 4 > 0)) {
				deckB.setNext_sequence_index(deckB.getCurrent_sequence_index()-1, 0);
			}
			println("[Deck B Seek Pattern]");
		}
		
		// Deck A Transpose
		if(number == ccTransposeA) {
			deckA.setTranspose(-1, (int)map(value, 0, 127, -12, 12));
			translideA.setValue(map(value, 0, 127, -12, 12)+12);
		}
		// Deck B Transpose
		if(number == ccTransposeB) {
			deckB.setTranspose(-1, (int)map(value, 0, 127, -12, 12));
			translideB.setValue(map(value, 0, 127, -12, 12)+12);
		}
		// AVAILABLE
		if ( number == 23 && altbutton) {
			
		}
		if ( number == 27 && altbutton) {
			
		}
		

		
		//RESET BPM TEMPO
		if ( number == ccResetTempoA && !altbutton) {
			deckA.setTempo(deckA.initialtempo);	
			temposliderA.setValue(deckA.initialtempo);
			println("[Deck A Reset Tempo]");
		}
		if ( number == ccResetTempoB && !altbutton) {
			deckB.setTempo(deckB.initialtempo);
			temposliderB.setValue(deckB.initialtempo);
			println("[Deck B Reset Tempo]");
		}

		
		
		// Deck A loopsegment TRIGGER
		if ( number == ccPattLoopTrigA && !altbutton){
			if (loopcurrentsegment == true){
				loopcurrentsegment = false;
				loopbt.override = false;
			} else {
				loopcurrentsegment = true;
				loopbt.override = true;
				loopcaught = deckA.player.ibxm.current_row;
				println(loopcaught);
			}
			println("[Deck A Sub-Pattern Loop TRIGGER]");
		}

		// Deck A loopsegment TOGGLE
		if ( number == ccPattLoopTogA && !altbutton){
			if (loopcurrentsegment == true){
				loopcurrentsegment = false;
				loopbt.override = false;
			} else {
				loopcurrentsegment = true;
				loopbt.override = true;
				loopcaught = deckA.player.ibxm.current_row;
				println(loopcaught);
			}
			println("[Deck A Sub-Pattern Loop TOGGLE]");
		}

		

		// Deck B loopsegment TRIGGER
		if ( number == ccPattLoopTrigB && !altbutton){
			if (loopcurrentsegmentB == true){
				loopcurrentsegmentB = false;
				loopbtB.override = false;
			} else {
				loopcurrentsegmentB = true;
				loopbtB.override = true;
				loopcaughtB = deckB.player.ibxm.current_row;
				println(loopcaughtB);
			}
			println("[Deck B Sub-Pattern Loop TRIGGER]");
		}

		// Deck B loopsegment TOGGLE
		if ( number == ccPattLoopTogB && !altbutton){
			if (loopcurrentsegmentB == true){
				loopcurrentsegmentB = false;
				loopbtB.override = false;
			} else {
				loopcurrentsegmentB = true;
				loopbtB.override = true;
				loopcaughtB = deckA.player.ibxm.current_row;
				println(loopcaughtB);
			}
			println("[Deck B Sub-Pattern Loop TOGGLE]");
		}
		

		// Deck A and B Channel Volume (chans 1-4 for each)
		
		if ( number == ccChanvolA1) {
			deckA.setChanvol(0, (int)map(value, 0, 127, 0, 48));
			println("[Deck A Channel 1 Volume]");
		}
		if ( number == ccChanvolA2) {
			deckA.setChanvol(1, (int)map(value, 0, 127, 0, 48));
			println("[Deck A Channel 2 Volume]");
		}
		if ( number == ccChanvolA3) {
			deckA.setChanvol(2, (int)map(value, 0, 127, 0, 48));
			println("[Deck A Channel 3 Volume]");
		}
		if ( number == ccChanvolA4) {
			deckA.setChanvol(3, (int)map(value, 0, 127, 0, 48));
			println("[Deck A Channel 4 Volume]");
		}
		if ( number == ccChanvolB1) {
			deckB.setChanvol(0, (int)map(value, 0, 127, 0, 48));
			println("[Deck B Channel 1 Volume]");
		}
		if ( number == ccChanvolB2) {
			deckB.setChanvol(1, (int)map(value, 0, 127, 0, 48));
			println("[Deck B Channel 2 Volume]");
		}
		if ( number == ccChanvolB3) {
			deckB.setChanvol(2, (int)map(value, 0, 127, 0, 48));
			println("[Deck B Channel 3 Volume]");
		}
		if ( number == ccChanvolB4) {
			deckB.setChanvol(3, (int)map(value, 0, 127, 0, 48));
			println("[Deck B Channel 4 Volume]");
		}
		
		// Deck A channel-muting (chans 1-4)
		
		if ( number == ccChanmuteA1 && !altbutton) {

			if (value == 0) {
				deckA.setChanmute(0, false);
			}

			if (value == 127) {
				deckA.setChanmute(0, true);
			}

		}
		if ( number == ccChanmuteA2 && !altbutton) {

			if (value == 0) {
				deckA.setChanmute(1, false);
			}

			if (value == 127) {
				deckA.setChanmute(1, true);
			}

		}
		if ( number == ccChanmuteA3 && !altbutton) {

			if (value == 0) {
				deckA.setChanmute(2, false);
			}

			if (value == 127) {
				deckA.setChanmute(2, true);
			}

		}
		if ( number == ccChanmuteA4 && !altbutton) {

			if (value == 0) {
				deckA.setChanmute(3, false);
			}

			if (value == 127) {
				deckA.setChanmute(3, true);
			}

		}


		// Deck B channel-muting (chans 1-4)
		if ( number == ccChanmuteB1 && !altbutton) {

			if (value == 0) {
				deckB.setChanmute(0, false);
			}

			if (value == 127) {
				deckB.setChanmute(0, true);
			}

		}
		if ( number == ccChanmuteB2 && !altbutton) {

			if (value == 0) {
				deckB.setChanmute(1, false);
			}

			if (value == 127) {
				deckB.setChanmute(1, true);
			}

		}
		if ( number == ccChanmuteB3 && !altbutton) {

			if (value == 0) {
				deckB.setChanmute(2, false);
			}

			if (value == 127) {
				deckB.setChanmute(2, true);
			}

		}
		if ( number == ccChanmuteB4 && !altbutton) {

			if (value == 0) {
				deckB.setChanmute(3, false);
			}

			if (value == 127) {
				deckB.setChanmute(3, true);
			}

		}
		
		// DECK A PREV MODULE
		if(number == 33 && altbutton && value == 127) {
				if(playlistposA - 1 >= 0) {
					currentsong = playlistposA-1;
					modLoader(playlistCP5_a.getItem(playlistposA-1).getName(), true,volumelocal, 0, playlistposA-1);				
					//println("Now playing: " + tempentry.content);
					if (deckA.loadSuccess > 0) {
						displayInfo(1, 'a');
						temposliderA.setValue(deckA.initialtempo);
						translideA.setValue(12);
					}
					playbt.btimg = loadImage("controls_04BIG.png");
				}
		}
		// DECK A NEXT MODULE
		if(number == 34 && altbutton && value == 127) {
			if(playlistposA+1 < playlistCP5_a.getListBoxItems().length) {
				currentsong = playlistposA+1;
				modLoader(playlistCP5_a.getItem(playlistposA+1).getName(), true,volumelocal, 0, playlistposA+1);				
				//println("Now playing: " + tempentry.content);
				if (deckA.loadSuccess > 0) {
					displayInfo(1, 'a');
					temposliderA.setValue(deckA.initialtempo);
					translideA.setValue(12);
				}
				playbt.btimg = loadImage("controls_04BIG.png");
			}
		}
		// DECK B PREV MODULE
		if(number == 37 && altbutton && value == 127) {
				if(playlistposB - 1 >= 0) {
					currentsongB = playlistposB-1;
					modLoader(playlistCP5_b.getItem(playlistposB-1).getName(), true,volumelocalB, 1, playlistposB-1);				
					//println("Now playing: " + tempentry.content);
					if (deckB.loadSuccess > 0) {
						displayInfo(1, 'b');
						temposliderB.setValue(deckB.initialtempo);
						translideB.setValue(12);
					}
					playbtB.btimg = loadImage("controls_04BIG.png");
				}
		}
		if(number == 38 && altbutton && value == 127) {
			if(playlistposB+1 < playlistCP5_b.getListBoxItems().length) {
				currentsongB = playlistposB+1;
				modLoader(playlistCP5_b.getItem(playlistposB+1).getName(), true,volumelocalB, 1, playlistposB+1);				
				//println("Now playing: " + tempentry.content);
				if (deckB.loadSuccess > 0) {
					displayInfo(1, 'b');
					temposliderB.setValue(deckB.initialtempo);
					translideB.setValue(12);
				}
				playbtB.btimg = loadImage("controls_04BIG.png");
			}
		}
		
		cueFix();
	}

	public void modLoader(String path, boolean autostart, int volume, int deck, int listpos) {
		// 0 == deckA, 1 == deckB
		if(deck < 1) {
			deckA.doModLoad(path, autostart, volume);			
			playlistposA = listpos;
			if(deckAcue) {
				for (int i=0; i < deckA.numchannels; i++) {
					deckA.player.ibxm.channels[i].set_panning(255);
				}
			} else {
				for (int i=0; i < deckA.numchannels; i++) {
					deckA.player.ibxm.channels[i].set_panning(0);
				}
			}
		} else {
			deckB.doModLoad(path, autostart, volume);
			playlistposB = listpos;
			if(deckBcue) {
				for (int i=0; i < deckB.numchannels; i++) {
					deckB.player.ibxm.channels[i].set_panning(255);
				}
			} else {
				for (int i=0; i < deckB.numchannels; i++) {
					deckB.player.ibxm.channels[i].set_panning(0);
				}
			}
		}
	}
	
	public String[] listFileNames(String dir) {
		File file = new File(dir);
		if (file.isDirectory()) {
			String names[] = file.list();
			return names;
		} else {
			// If it's not a directory
			return null;
		}
	}
			
	public void noteOn(int channel, int pitch, int velocity, String bus_name) {
		println("NoteBusname: " + NoteBusname + "    bus_name: " + bus_name);
		if(bus_name == NoteBusname) {
			println("NoteOn - Pitch: " + pitch + " Velocity: " + velocity);
			println(hex((int)map(velocity, 0, 127, 0, 64),2));
			int vel = 0;
			if (velocity < 64) {
				vel = 32;
			} else {
				vel = (int)map(velocity, 64, 127, 32, 64);
			}
			//just a little dynamic range 
			if(liveinstrument[0] >= 0) {
				deckA.customkeyDown(pitch + transpose, liveinstrument[0], hex(vel, 2), "0", "00");
			}
			if(liveinstrument[1] >= 0) {
				deckB.customkeyDown(pitch + transpose, liveinstrument[1], hex(vel, 2), "0", "00");
			}
		}

		//jam = deckA.jamcounter;
	}

	public void noteOff(int channel, int pitch, int velocity, String bus_name) {
		if(bus_name == NoteBusname) {
			println("NoteOff - Pitch: " + pitch + " Velocity: " + velocity);
			if(liveinstrument[0]>=0) {
				deckA.customkeyUp(pitch, liveinstrument[0]);
				deckA.customkeyUp(pitch+transpose, liveinstrument[0]);
			}
			if(liveinstrument[1]>=0){
				deckB.customkeyUp(pitch, liveinstrument[1]);
				deckB.customkeyUp(pitch+transpose, liveinstrument[1]);
			}
		}
	}
	
	public void updateControlcolours() {
		//volumesliderA.setColorBackground(0xffffffff);
		volumesliderA.setColorForeground(foregroundcol);
		volumesliderA.setColorActive(foreshadecol);
		//volumesliderB.setColorBackground(0xffffffff);
		volumesliderB.setColorForeground(foregroundcol);
		volumesliderB.setColorActive(foreshadecol);
		//temposliderA.setColorBackground(0xffffffff);
		temposliderA.setColorForeground(foregroundcol);
		temposliderA.setColorActive(foreshadecol);
		//temposliderB.setColorBackground(0xffffffff);
		temposliderB.setColorForeground(foregroundcol);
		temposliderB.setColorActive(foreshadecol);
		//crossfadeslider.setColorBackground(0xffffffff);
		crossfadeslider.setColorForeground(foregroundcol);
		crossfadeslider.setColorActive(foreshadecol);
		//translideA.setColorBackground(0xffffffff);
		translideA.setColorForeground(foregroundcol);
		translideA.setColorActive(foreshadecol);
		//translideB.setColorBackground(0xffffffff);
		translideB.setColorForeground(foregroundcol);
		translideB.setColorActive(foreshadecol);
		
		crossfaderbehaviour.setColorForeground(foregroundcol);
		crossfaderbehaviour.setColorActive(foreshadecol);
		slicebehaviour.setColorForeground(foregroundcol);
		slicebehaviour.setColorActive(foreshadecol);
				
		midi2.setColorForeground(foregroundcol);
		midi3.setColorForeground(foregroundcol);		
		playlistCP5_a.setColorForeground(foregroundcol);
		playlistCP5_a.setColorActive(foreshadecol);
		playlistCP5_b.setColorForeground(foregroundcol);
		playlistCP5_b.setColorActive(foreshadecol);		
		midi2.setColorActive(foregroundcol);
		midi3.setColorActive(foregroundcol);		
		
	}
	
	
	public void doListAadd(File selection) {
		if(selection!= null) {
			String loadpath = selection.getAbsolutePath();
			if(deckA.headerCheck(loadpath)) {
				String tempname = loadpath.substring(loadpath.lastIndexOf('/')).substring(1);
				playlistCP5_a.addItem(loadpath, playlistCP5_a.getListBoxItems().length);
				playlistCP5_a.getItem(loadpath).setText(tempname);
			}
		}
						
	}
	
	public void doListBadd(File selection) {
		if(selection!= null) {
			String loadpath = selection.getAbsolutePath();
			if(deckB.headerCheck(loadpath)) {
				String tempname = loadpath.substring(loadpath.lastIndexOf('/')).substring(1);
				playlistCP5_b.addItem(loadpath, playlistCP5_b.getListBoxItems().length);
				playlistCP5_b.getItem(loadpath).setText(tempname);
			}
		}
						
	}
	
	public void doLoadPlaylistA(File selection) {
		loadm3uPlaylist('a', selection.getAbsolutePath());
	}
	
	public void doLoadPlaylistB(File selection) {
		loadm3uPlaylist('b', selection.getAbsolutePath());
	}
	
	public void controlEvent(ControlEvent theEvent) {
		cueFix();
		//println(theEvent.name());
		if(theEvent.isGroup() && theEvent.getName().equals("playlist_a")){
			// CHECK FOR DOUBLE-CLICK!
			if (mouseEvent.getClickCount() == 2) {
				if (mouseButton == LEFT) {
					//deckA.doModLoad(playlistCP5_a.getItem((int)theEvent.group().value()).getgetName(), true, volumelocal);
					modLoader(playlistCP5_a.getItem((int) theEvent.getGroup().getValue()).getName(), true,volumelocal, 0, (int) theEvent.getGroup().getValue());
					currentsong = (int) theEvent.getGroup().getValue();
					//println("Now playing: " + tempentry.content);
					if (deckA.loadSuccess > 0) {
						displayInfo(1, 'a');
						temposliderA.setValue(deckA.initialtempo);
						translideA.setValue(12);
					}
					playbt.btimg = loadImage("controls_04BIG.png");
				} 
			} else {
				selectedTuneA = playlistCP5_a.getItem((int) theEvent.getGroup().getValue()).getName();
			}
			
			
		}
		
		if(theEvent.isGroup() && theEvent.getName() == "playlist_b") {
			// CHECK FOR DOUBLE-CLICK!
			if (mouseEvent.getClickCount() == 2) {
				//deckB.doModLoad(playlistCP5_b.getItem((int)theEvent.group().value()).getName(), true, volumelocalB);
				modLoader(playlistCP5_b.getItem((int)theEvent.getGroup().getValue()).getName(), true, volumelocalB, 1, (int)theEvent.getGroup().getValue());
				currentsongB = (int)theEvent.getGroup().getValue();
				//println("Now playing: " + tempentry.content);
				if (deckB.loadSuccess > 0) {
					displayInfo(1, 'b');
					temposliderB.setValue(deckB.initialtempo);
					translideB.setValue(12);
				}
				playbtB.btimg = loadImage("controls_04BIG.png");
			}
			 else {
					selectedTuneB = playlistCP5_b.getItem((int) theEvent.getGroup().getValue()).getName();
				}
		}
		

		
		if(theEvent.getName() == "listAadd") {						
			
			selectInput("Choose a MOD, S3M or XM file to add to Deck A's playlist...", "doListAadd");
			
		}

		if(theEvent.getName() == "listBadd") {
			selectInput("Choose a MOD, S3M or XM file to add to Deck B's playlist...", "doListBadd");
		}


		if(theEvent.getName() == "listAloadm3u") {			
			selectInput("Choose an m3u playlist file to load", "doLoadPlaylistA");			
		}
		if(theEvent.getName() == "listBloadm3u") {
			selectInput("Choose an m3u playlist file to load", "doLoadPlaylistB");
		}
		if(theEvent.getName() == "listAsavem3u") {
			savePlaylistToFile('a');
		}
		if(theEvent.getName() == "listBsavem3u") {
			savePlaylistToFile('b');
		}
		
		if(theEvent.getName() == "intTransposeA") {
			if(deckA.loadSuccess > 0 && deckA.playing) {
				deckA.setTranspose(-1, (int)map(theEvent.getValue(), 0, 24, -12, 12));
				transpose = (int)map(theEvent.getValue(), 0, 24, -12, 12);
				transpose = (int)map(theEvent.getValue(), 0, 24, -12, 12);
				deckA.setTranspose(-1, transpose);
			}
		}
		if(theEvent.getName() == "intTransposeB") {
			if(deckB.loadSuccess > 0 && deckB.playing) {
				deckB.setTranspose(-1, (int)map(theEvent.getValue(), 0, 24, -12, 12));
				transposeB = (int)map(theEvent.getValue(), 0, 24, -12, 12);
			}
		}
	
		if(theEvent.getName() == "cuemixvolume") {
			cuevolume = (int)theEvent.getValue();
		}
		
		if(theEvent.getName() == "intVolumeA") {
			doVolume();
		}
		if(theEvent.getName() == "intVolumeB") {
			doVolume();
		}
		
		if(theEvent.getName() == "listAremove") {

		}
		if(theEvent.getName() == "listBremove") {

		}
		
		if(theEvent.getName() == "intTempoA") {
			if(deckA.loadSuccess > 0) {deckA.setTempo((int)theEvent.getValue());}
		}
		if(theEvent.getName() == "intTempoB") {
			if(deckB.loadSuccess > 0) {deckB.setTempo((int)theEvent.getValue());}
		}
		
		if(theEvent.getName() == "intCrossfade") {
			doVolume();
			cueFix();
		}
				
		if (theEvent.isGroup() && theEvent.getName() == "midi2") {
			// an event from a group e.g. scrollList
			println("MIDI device change: " + (int)theEvent.getGroup().getValue());	
			
			MidiBus.list();
			String[][] list_of_devices = MidiBus.returnList();			
			if ((int)theEvent.getGroup().getValue() < list_of_devices.length){
				if(midiIndevice < 0) {
					midiIndevice = (int)theEvent.getGroup().getValue();
				}
				println("Current MIDI-in device: " + midiIndevice);
				myBus.clearAll();
				// 'list_of_devices.length' will always be out of bounds, but won't cause exception
				myBus = new MidiBus(this, midiIndevice, list_of_devices.length);
				ControlBusname = noteInputbus.getBusName();
			}			
			midi2.setLabel("midi device");
		}
		
		if (theEvent.isGroup() && theEvent.getName() == "midi3") {
			// an event from a group e.g. scrollList
			println("MIDI note device change: " + (int)theEvent.getGroup().getValue());				
			String[][] list_of_note_devices = MidiBus.returnList();			
			if ((int)theEvent.getGroup().getValue() < list_of_note_devices.length){
				if(midiNotedevice < 0) {
					midiNotedevice = (int)theEvent.getGroup().getValue();
				}
				println("Current MIDI-note-in device: " + midiNotedevice);
				noteInputbus.clearAll();
				// 'list_of_devices.length' will always be out of bounds, but won't cause exception
				noteInputbus = new MidiBus(this, midiNotedevice, list_of_note_devices.length);
				NoteBusname = noteInputbus.getBusName();
			}			
			midi3.setLabel("MIDI Note Device");
		}

		
		if(theEvent.getName() == "crossfadebehaviour") {
			try {
				crossbehaviour = (int)theEvent.getGroup().getValue();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}	
		
		if(theEvent.getName() == "slicebehaviour") {
			try {
				slicetriggerbehaviour = (int)theEvent.getGroup().getValue();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		cueFix();  
	}
	
	public void doVolume(){
		if (deckA.loadSuccess > 0 && deckB.loadSuccess > 0) {
			
			int deckAoverallVol = (int) volumesliderA.getValue(); // between 0 and 48
			int deckBoverallVol = (int) volumesliderB.getValue(); // between 0 and 48
			int crossfaderposition = (int) crossfadeslider.getValue(); // 0 and 96: 0-64 for deck A, 24-96 for deck B.
			
			if (crossbehaviour == 0) {
				// I'll have to check for crossfader behaviour here and adjust the calculations accordingly
				if (crossfaderposition > 48) { // deckA volume should fall off to zero beyond 64; deckB should be 100%
					deckAoverallVol = (int) map(crossfaderposition, 48, 96,
							(int) volumesliderA.getValue(), 0);
				}
				if (crossfaderposition < 48) { // Crossfader
					deckBoverallVol = (int) map(crossfaderposition, 48, 0,
							(int) volumesliderB.getValue(), 0);
				}
			}
			
			if (crossbehaviour == 1) {
				// BEATMATCH behaviour - e.g. A scales from 100% at the left to 0% at the right
				float inputvalA = map(crossfaderposition, 0, 96, 0, 1);
				float inputvalB = map(crossfaderposition, 96, 0, 0, 1);
				float inputlogA = log(1-inputvalA);
				float inputlogB = log(1-inputvalB);
				deckAoverallVol = (int) map(inputlogA, 0f, -4f, 48, 0);				
				deckBoverallVol = (int) map(inputlogB, 0f, -4f, 48, 0);
			}
						
			
			if (crossbehaviour == 2) {
				// A-BIAS CUT
				if (crossfaderposition > 8) { // deckA volume should fall off to zero beyond 64; deckB should be 100%
					deckBoverallVol = (int) volumesliderB.getValue();
					deckAoverallVol = 0;					
				} else {
					deckAoverallVol = (int) volumesliderA.getValue();
					deckBoverallVol = 0;
				}				
			}
			
			if (crossbehaviour == 3) {
				// B-BIAS CUT
				if (crossfaderposition > 88) { // deckA volume should fall off to zero beyond 64; deckB should be 100%
					deckBoverallVol = (int) volumesliderB.getValue();
					deckAoverallVol = 0;
				} else {
					deckAoverallVol = (int) volumesliderA.getValue();
					deckBoverallVol = 0;					
				}
			}
			
			if (crossbehaviour == 4) {
				// I'll have to check for crossfader behaviour here and adjust the calculations accordingly

					deckAoverallVol = (int) map(crossfaderposition, 0, 96,
							(int) volumesliderA.getValue(), 0);

					deckBoverallVol = (int) map(crossfaderposition, 96, 0,
							(int) volumesliderB.getValue(), 0);
			}
			
			if (!deckAcue) {
				deckA.setGlobvol(deckAoverallVol);
				volumelocal = deckAoverallVol; // We set this so that subsequent modLoads can init at the same volume
			} else {
				deckA.setGlobvol(cuevolume);
			}
			if (!deckBcue) {
				deckB.setGlobvol(deckBoverallVol);
				volumelocalB = deckBoverallVol; // We set this so that subsequent modLoads can init at the same volume
			} else {
				deckB.setGlobvol(cuevolume);
			}
		}
		
	}
	
	public void cueFix() {
		// Should restore cue status according to each bool 
		if(deckAcue) {
			cuedeck('a', true);					
		} else {
			cuedeck('a', false);
		}
		if(deckBcue) {
			cuedeck('b', true);					
		} else {
			cuedeck('b', false);
		}		
	}
		
	public void loadm3uPlaylist(char deck, String path) {
			if (deck == 'a') {
				if (loadStrings(path) != null) {
					String m3ulines[] = loadStrings(path);
						for (int i = 0; i < m3ulines.length; i++) {
							if (match(m3ulines[i], "#") == null) {
								//continue - we only want lines with no hashes at the beginning
								String temppath = StringEscapeUtils.escapeJava(m3ulines[i]);
								String tempname = temppath.substring(temppath.lastIndexOf('/')).substring(1);
								if (deckA.headerCheck(temppath)) {
									playlistCP5_a.addItem(temppath, playlistCP5_a.getListBoxItems().length);
									playlistCP5_a.getItem(temppath).setText(tempname);									
								}
							} 						
					}
				}
			}
			if (deck == 'b') {
				if (loadStrings(path) != null) {
					String m3ulines[] = loadStrings(path);
						for (int i = 0; i < m3ulines.length; i++) {
							if (match(m3ulines[i], "#") == null) {
								//continue - we only want lines with no hashes at the beginning
								String temppath = StringEscapeUtils.escapeJava(m3ulines[i]);
								String tempname = temppath.substring(temppath.lastIndexOf('/')).substring(1);
								if (deckB.headerCheck(temppath)) {
									playlistCP5_b.addItem(temppath, playlistCP5_b.getListBoxItems().length);
									playlistCP5_b.getItem(temppath).setText(tempname);									
								}
							} 						
					}
				}
			}

		
	}

	
	public static void copyInputStream(InputStream in, OutputStream out) throws IOException {
	      byte[] buffer = new byte[1024];
	      int len = in.read(buffer);
	      while (len >= 0) {
	          out.write(buffer, 0, len);
	          len = in.read(buffer);
	      }
	      in.close();
	      out.close();
	  }
	
	
	
	class ChipdiscoDropListenerA extends DropListener {

		public char currentdeck;

		ChipdiscoDropListenerA() {
			setTargetRect(playlistCP5_a.getPosition().x, playlistCP5_a.getPosition().y, playlistCP5_a.getPosition().x + playlistCP5_a.getWidth(), (playlistCP5_a.getPosition().y + 195));
		}

		public void dropEvent(DropEvent theEvent) {
			println("File was dropped in playlist A: " + theEvent.filePath());
			String ext = theEvent.filePath().substring(theEvent.filePath().length()-3, theEvent.filePath().length());
			if (ext.equalsIgnoreCase("m3u")){
				loadm3uPlaylist('a', theEvent.filePath());
			} else {
				if(deckA.headerCheck(theEvent.filePath())) {
					String tempname = theEvent.filePath().substring(theEvent.filePath().lastIndexOf('/')).substring(1);
					playlistCP5_a.addItem(theEvent.filePath(), playlistCP5_a.getListBoxItems().length);
					playlistCP5_a.getItem(theEvent.filePath()).setText(tempname);
				}
			}	
		}
	}
	
	class ChipdiscoDropListenerB extends DropListener {

		public char currentdeck;

		ChipdiscoDropListenerB() {
			setTargetRect(playlistCP5_b.getPosition().x, playlistCP5_b.getPosition().y, playlistCP5_b.getPosition().x + playlistCP5_b.getWidth(), (playlistCP5_b.getPosition().y + 195));
		}

		public void dropEvent(DropEvent theEvent) {
			String ext2 = theEvent.filePath().substring(theEvent.filePath().length()-3, theEvent.filePath().length());
			if (ext2.equalsIgnoreCase("m3u")){
				loadm3uPlaylist('b', theEvent.filePath());
			} else {
				if(deckB.headerCheck(theEvent.filePath())) {
					String tempname = theEvent.filePath().substring(theEvent.filePath().lastIndexOf('/')).substring(1);
					playlistCP5_b.addItem(theEvent.filePath(), playlistCP5_b.getListBoxItems().length);
					playlistCP5_b.getItem(theEvent.filePath()).setText(tempname);
				}
			}	
		}
	}
	
	
	public void stop() {
		deckA.stop();
		deckB.stop();
		super.stop();
	}


	
}