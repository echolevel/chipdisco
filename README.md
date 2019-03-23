 * Chipdisco v6 by Brendan Ratliff - http://echolevel.co.uk
 * 
 * A dual-deck DJ application for mixing Amiga MOD files.
 * 
 * Based on IBXM by Martin Cameron (© 2008) 
 * 

 SETUP:
 No installation is needed, but to start Chipdisco with your chosen preferences preloaded, just copy the file chipdisco_config.txt 
 to your home directory:

 ~/username or /home/username on Linux
 ~/username or /Users/username on OS X
 <root>\Documents and Settings\username on Windows XP and
 <root>\Users\username on Windows Vista and 7

 then edit it if you want (the options are all explained in the file), then run Chipdisco6!

 QUICK START GUIDE: load modules into either deck with with ADD or by dragging/dropping them onto the decks 
 from your computer. Both decks default to CUE MIX mode (see below) and will play out of your right speaker or headphone
 when you double-click the file in the playlist. CUE MIX VOLUME adjusts the overall listening volume. The BPM slider
 adjusts the module's tempo while the small slider in the centre of the app transposes the pitch of the module up or down
 by 12 semitones each way. The '+' and '-' buttons are for fine adjustments of the BPM. The '!' button on each deck forces the 
 *other* deck's BPM to match the first deck's BPM. Hitting spacebar will force both modules to jump to pattern 0 - if the BPMs
 are the same, this should theoretically synchronise them. Use with care :) 
 
 Once you're ready to send a deck to main playout (the left audio channel - in a live situation you'd plug
 this into the PA from a DJ splitter cable or similar), hit the button with the speaker icon to exit CUE MIX mode for that deck.
 Once a deck is in PLAYOUT or FOH (front of house) mode, the crossfader will affect its volume in the normal way. The big volume
 faders in the centre set overall maximum levels for each deck's playout.

 Have fun! DJ those chiptunes and doskpop classics until somebody asks you to stop! And then keep doing it! For ever!



 * What's changed? Loads of stuff, particularly:
 * 
 * # NO MORE SINGLE/UNIFIED PLAYLIST! 
 *   This is a good idea for most MP3 DJ apps, but was baffling unintuitive for Chipdisco.
 * 
 * # GUI SIZE INCREASED BY 150%!
 *   It was difficult to see on most screens and now it fills a 1280x800 screen (like my Macbook's, for instance) perfectly
 * 
 * # NO MORE BROWSER APPLET VERSION! 
 *   Processing are ditching support for this and applet security keeps getting tighter and fiddlier. And Chipdisco 
 *   doesn't *need* to be a browser app, let's face it.
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