import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.InventoryListener;
import org.dreambot.api.script.listener.MessageListener;
import org.dreambot.api.script.listener.PaintListener;
import org.dreambot.api.methods.container.impl.bank.*;
import org.dreambot.api.script.Category;
import org.dreambot.api.methods.skills.*;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.world.*;
import org.dreambot.api.Client;
import org.dreambot.api.input.Mouse;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

class Rect{
	public int x,y,width,height;
	Rect(int x, int y, int width, int height){
		this.x=x; this.y=y; 
		this.width=width; this.height=height;
	}
	public boolean getClicked(MouseEvent mouseEv) {
		int mouseX = mouseEv.getX(); int mouseY = mouseEv.getY();
		if(mouseX > x && mouseX < x+width) {
			if(mouseY > y && mouseY < y+height) {
				return true;
			}
		}
		return false;
	}
}
class MouseThread extends Thread{
	Mouse mouse;
	MouseThread(Mouse maus){
		this.mouse = maus;
	}
	public void run() {
		MethodProvider.log("Mouse anti-AFK thread started...");
		while(true) {
			try {
				if(Calculations.random(0,101) > 50) {
					mouse.move();
				}
				else {
					mouse.moveMouseOutsideScreen();
				}
				Thread.sleep(Calculations.random(30000, 201000));
			} catch (InterruptedException e) {
				MethodProvider.log("Interrupted mouse anti-AFK thread...");
				MethodProvider.log(e.toString());
				break;
			}
		}
	}
}

@ScriptManifest(author = "Adam Brewer", category = Category.WOODCUTTING,
        name = "Caelestis' Woodcutter", description = "Chops anywhere, banks anywhere.", version = 1.0)


public class Woodcutter extends AbstractScript implements PaintListener, MessageListener {
	private GUI gui;
	private boolean statboardHidden = false;
	private GameObjects objList;
	private Area startArea=null;
	private SkillTracker skillTracker=null;
	private long timeBegan, timeRan;
	private int logsCollected, dropsCollected;
	private List<String> possibleDrops;
	private Inventory playerInventory;
	private MouseThread mouseThread;
	private Rect closeBox = new Rect(492,344,21,21);
	private Rect openBox = new Rect(492,453,21,21);
    public void onStart() {
    	possibleDrops = new ArrayList<String>() {{
    		add("sulliuscep"); add("nest"); add("fungus"); add("mushroom");
    	}};
    	
    	timeBegan = System.currentTimeMillis();
    	
    	getWalking().setRunThreshold(40);
    	
    	log("Caelestis Woodcutter");
    	log("Initializing GUI...");
    	mouseThread = new MouseThread(getMouse());
    	gui = new GUI();
    	gui.setVisible(true);
    }
    @Override
    public void onGameMessage(Message message) {
    	String msg = message.toString().toLowerCase();
    	if(msg.contains("log") && msg.contains("you get some")) {
    		logsCollected++;
    	}
    }
    private void ChanceServerChange() {
    	int hopChance = Calculations.random(0,555);
    	//log("Required choice: ".concat("999"));
    	//log("Random choice: ".concat(Integer.toString(hopChance)));
    	//DEBUG
    	if(hopChance == 20) {
    		boolean isMember = Client.getClient().isMembers();
    		int totalLevel = getSkills().getTotalLevel();
    		int currentWorld = Client.getClient().getCurrentWorld();
    		List<World> worldPool;
    		List<World> filteredWorldPool = new ArrayList<World>();
    		//	Grab world pool based on membership
    		if(isMember) {
    			log("Player is a member!");
    			worldPool = getWorlds().members();
    		}
    		else {
    			log("Player is not a member!");
    			worldPool = getWorlds().f2p();
    		}
    		//	Filter world pool based on level
    		for(int i = 0; i < worldPool.size(); i++) {
    			World cWorld = worldPool.get(i);
    			int worldMinLevel = cWorld.getMinimumLevel();
    			log("It World: #".concat(Integer.toString(cWorld.getID())));
    			log("Current World: #".concat(Integer.toString(currentWorld)));
    			if(worldMinLevel <= totalLevel && cWorld.getID() != currentWorld) {
    				filteredWorldPool.add(cWorld);
    			}
    		}
    		
    		// Remove world the player is in
    		//	Select world to hop to from filtered world pool
    		int worldSel = Calculations.random(0,filteredWorldPool.size());
    		log("Hopping to world ".concat(Integer.toString(filteredWorldPool.get(worldSel).getID())));
    		getWorldHopper().hopWorld(filteredWorldPool.get(worldSel));
    	}
    }
    private void walkTo(Area location) {
    	while(!location.contains(getLocalPlayer())) {
			getWalking().walk(location.getRandomTile());
			sleep(1001,1569);
		}
    }
   
    private void depositLogs() {
    	Bank bank = getBank();
		while(!bank.isOpen()) {
			bank.openClosest();
			log("Waiting to open bank...");
			sleep(Calculations.random(462,1242));
		}
		bank.depositAllExcept(item -> item != null && item.getName().toLowerCase().contains("axe"));
		bank.close();
    }
    private String formatTime(long duration)

    {
    	String res = "";
    	String a,b,c;
    	long days = TimeUnit.MILLISECONDS.toDays(duration);
    	long hours = TimeUnit.MILLISECONDS.toHours(duration) - 
    			TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
    	long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) -
    			TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
    	long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
    			- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
    	if(hours < 10) {
    		a = "0";
    	}else { a = ""; }
    	
    	if(minutes < 10) {
    		b="0";
    	}else { b=""; }
    	
    	if(seconds < 10) {
    		c="0";
    	}else { c=""; }
    	if (days == 0) {
    		res = (a+hours + ":" + b+minutes + ":" + c+seconds);
    	} 
    	else {
    		res = (days + ":" + a+hours + ":" + b+minutes + ":" + c+seconds);
    	}
    	return res;

    }
    private long logsPerHour() {
    	return (logsCollected*3600000)/timeRan;
    }
    
    @Override
    public void onMouse(MouseEvent mouseEv) {
    	log("Mousey wousey");
    	if((statboardHidden && openBox.getClicked(mouseEv)) || (!statboardHidden && closeBox.getClicked(mouseEv))) {
    		statboardHidden = !statboardHidden;
    	}
    }
    
    @Override
    public void onPaint(Graphics g) {
    	this.timeRan = System.currentTimeMillis() - this.timeBegan;
    	Image statboard = null;
    	Image openBoxImage = null;
    	Image crosshair = null;
    	try {
    		statboard = ImageIO.read(getClass().getResourceAsStream("/statboard.jpg"));
    		openBoxImage = ImageIO.read(getClass().getResourceAsStream("/open.jpg"));
    		crosshair = ImageIO.read(getClass().getResourceAsStream("/crosshair.png"));
    	}
    	catch (IOException e) {
    		log("Failed to load Statboard images!");
    	}
    	g.setColor(Color.RED);
//    	Draw mouse
    	if(!statboardHidden) {
	    	g.drawImage(statboard,0,338,null);
	    	//	Draw first column of statistics
	    	g.drawString(formatTime(this.timeRan), 90, 367);
	    	g.drawString(formatTime(skillTracker.getTimeToLevel(Skill.WOODCUTTING)),90,390);
	    	g.drawString(Long.toString(skillTracker.getGainedExperience(Skill.WOODCUTTING)), 90, 413);
	    	g.drawString(Long.toString(skillTracker.getGainedExperiencePerHour(Skill.WOODCUTTING)), 90, 435);
	    	g.drawString(Integer.toString(getSkills().getExperienceToLevel(Skill.WOODCUTTING)),90,458);
	    	//	Draw second column of statistics
	    	g.drawString(Integer.toString(logsCollected), 333, 367);
	    	g.drawString(Long.toString(logsPerHour()),333,390);
	    	g.drawString(Integer.toString(dropsCollected), 333, 413);
	    	Point mousePos = getMouse().getPosition();
	    	g.drawImage(crosshair,mousePos.x-13, mousePos.y-13, null);
    	}
    	else {
    		g.drawImage(openBoxImage,492,453,null);
    		Point mousePos = getMouse().getPosition();
        	g.drawImage(crosshair,mousePos.x-13, mousePos.y-13, null);
    	}
    	
    	
    }
    
    @Override
    public void onExit() {
    	mouseThread.interrupt();
    	gui.setVisible(false);
    	//gui.close();
    	super.onExit();
    	stop();
    }
    
    private boolean nameContains(String name,String contains) {
    	return name.toLowerCase().contains(contains.toLowerCase());
    }
    
    private boolean nameContainsAny(String itemName, List<String> possibleNames) {
    	for(int i = 0; i < possibleNames.size(); i++) {
    		if(itemName.toLowerCase().contains(possibleNames.get(i).toLowerCase())) {
    			return true;
    		}
    	}
    	return false;
    }
    
    @Override
    public int onLoop() {
    	if(gui.started && Client.getClient().isLoggedIn()) {
    		if(skillTracker == null) {
    			skillTracker = getSkillTracker();
    	    	skillTracker.start(Skill.WOODCUTTING);
    		}
    		playerInventory = getInventory();
    		//	Dragon axe special
    		//log("Special pctg: ".concat(Integer.toString(getCombat().getSpecialPercentage())));
    		if(getEquipment().contains(item -> item != null && nameContains(item.getName(), "dragon axe"))) {
    			Combat combat = getCombat();
    			if(combat.getSpecialPercentage() == 100) {
    				combat.toggleSpecialAttack(true);
    			}
    		}
    		
    		//	Set area to return to
    		if(startArea == null) {
    			startArea = getLocalPlayer().getTile().getArea(2);
    		}
    		
    		if(gui.isVisible()) { 
    			gui.setVisible(false);
    		}
    		
    		objList = getGameObjects();
    		if(!getLocalPlayer().isAnimating()) {
    			log("Interrupting mouseThread!");
    			mouseThread.interrupt();
    	        GameObject tree = objList.closest(gui.tree);
    	        //	Inventory check, tree validity and distance check
    	        if (tree != null && !playerInventory.isFull() && tree.distance() <= gui.maxDistance) {
    	        	//	Check special
    	        	
    	            log("Rotating camera towards tree...");
    	        	getCamera().mouseRotateToTile(tree.getTile());
    	        	tree.interact("Chop down");
    	        } 
    	        else if (tree == null) {
    	        	log("Waiting for a tree to spawn...");
    	        }
    	        else if (playerInventory.isFull()) {
    	        	log("Inventory full!");
    	        	//	Banking
    	        	if(gui.bank) {
    	        		Area closestBank = getBank().getClosestBankLocation().getArea(1);
    	        		if(closestBank != null) {
    	        			//	Run to bank
    	        			walkTo(closestBank);
    	        			//	Bank items
    	        			log("Interacting with banker...");
    	        			depositLogs();
    	        			//	Return to cutting area
    	        			walkTo(startArea);
    	        		}
    	        		else {
    	        			log("Error finding nearest bank");
    	        		}
    	        	}
    	        	//	Power chopping
    	        	else {
        	            getInventory().dropAll(item -> item != null && nameContains(item.getName(), "log"));
    	        	}
    	        }
    	        //	World hop chance
    	        if(gui.worldHop) {
    	        	ChanceServerChange();
    	        }
    	        //	Collect drops
    	        if(gui.birdsNest && !playerInventory.isFull()) {
    	        	
    	        	GameObject takeable = objList.closest(item -> item != null && nameContainsAny(item.getName(), possibleDrops));
    	        	if(takeable != null && takeable.distance() <= 15) {
    	        		takeable.interact("Take");
    	        		dropsCollected++;
    	        	}
    	        }
        	}
    		else {
    			if(!mouseThread.isAlive()) {
    				log("Mouse thread dead...starting another!");
    				try {
    				mouseThread.start();
    				}
    				catch(Exception e) {
    					log("Failed to restart thread, creating new one...");
    					mouseThread = new MouseThread(getMouse());
    					mouseThread.start();
    				}
    			}
    		}
    		
    		return Calculations.random(453, 725);
    	}
    	else {
    		if(gui.gml.dragging) {
    			Point mousePoint = MouseInfo.getPointerInfo().getLocation();
    			gui.setLocation(new Point(
    					mousePoint.x - gui.gml.dragPoint.x,
    					mousePoint.y - gui.gml.dragPoint.y
    					));
    		}
    		return 5;
    	}
    }
	@Override
	public void onPlayerMessage(Message arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPrivateInMessage(Message arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPrivateOutMessage(Message arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onTradeMessage(Message arg0) {
		// TODO Auto-generated method stub
		
	}
    
}
