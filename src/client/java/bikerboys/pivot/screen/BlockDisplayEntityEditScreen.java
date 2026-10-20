package bikerboys.pivot.screen;

import bikerboys.pivot.PivotClient;
import bikerboys.pivot.Scheduler;
import bikerboys.pivot.networking.packets.*;
import bikerboys.pivot.util.Util;
import com.google.common.collect.ImmutableList;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static bikerboys.pivot.util.Util.eulerDegreesToQuaternion;
import static bikerboys.pivot.util.Util.quaternionToEulerDegrees;

public class BlockDisplayEntityEditScreen extends Screen {
	private static final int LABEL_X = 10;
	private static final int CONTROL_X = 80;
	private static final int ENTRY_HEIGHT = 22;
	private static final int ENTRY_WIDTH = 188;
	private static final int LIST_WIDTH = 220;
	private static final int MAX_LIST_HEIGHT = 130;
	private static final int MIN_LIST_HEIGHT = 44;
	private static final int OFFSET_BUTTON_WIDTH = 34;
	private static final double[] OFFSET_STEPS = {0.5, 0.25, 0.1, 0.05};
	private static final int LIST_PADDING = 5;

	private record Label(String text, int x, int y) {}

	private record StateEntry(String label, Runnable action) {}

	private ItemDisplayContext currentContext = ItemDisplayContext.NONE;
	private int tick = 0;
	private Display displayEntity;

	private final List<Label> labels = new ArrayList<>();
	private final List<ValueSlider> sliders = new ArrayList<>();
	private final List<Button> stateButtons = new ArrayList<>();

	private ValueSlider rotXSlider;
	private ValueSlider rotYSlider;
	private ValueSlider rotZSlider;
	private ValueSlider scaleXSlider;
	private ValueSlider scaleYSlider;
	private ValueSlider scaleZSlider;
	private Button lockButton;

	private int rowHeight = 24;
	private int listX;
	private int listY;
	private int listHeight = MAX_LIST_HEIGHT;
	private int listScroll;
	private int listMaxScroll;
	private String stateSignature = "";
	private String stateEntityUuid = "";

	public BlockDisplayEntityEditScreen() {
		super(Component.literal(""));
	}

	private Button button(String text, int x, int y, int width, Button.OnPress onPress) {
		return addRenderableWidget(Button.builder(Component.literal(text), onPress).bounds(x, y, width, 20).build());
	}

	private ValueSlider slider(int x, int y, double min, double max, Runnable onChange) {
		ValueSlider slider = addRenderableWidget(new ValueSlider(x, y, 140, min, max, onChange));
		sliders.add(slider);
		return slider;
	}

	private ValueSlider sliderRow(String label, int y, double min, double max, double reset, String resetText, double step, String stepText, Runnable onChange) {
		labels.add(new Label(label, LABEL_X, y + 6));
		ValueSlider slider = slider(CONTROL_X, y, min, max, onChange);
		button(resetText, CONTROL_X + 142, y, 20, press -> slider.setRealValue(reset, true));
		button("-" + stepText, CONTROL_X + 164, y, 36, press -> slider.nudge(-step));
		button("+" + stepText, CONTROL_X + 202, y, 36, press -> slider.nudge(step));
		return slider;
	}

	private void offsetRow(String label, int axis, int y) {
		labels.add(new Label(label, LABEL_X, y + 6));

		int x = CONTROL_X;
		for (int i = 0; i < OFFSET_STEPS.length; i++) {
			double step = OFFSET_STEPS[i];
			button("-" + formatStep(step), x, y, OFFSET_BUTTON_WIDTH, press -> shiftPosition(axis, -step));
			x += OFFSET_BUTTON_WIDTH + 2;
		}
		for (int i = OFFSET_STEPS.length - 1; i >= 0; i--) {
			double step = OFFSET_STEPS[i];
			button("+" + formatStep(step), x, y, OFFSET_BUTTON_WIDTH, press -> shiftPosition(axis, step));
			x += OFFSET_BUTTON_WIDTH + 2;
		}
	}

	private static String formatStep(double step) {
		return String.valueOf(step);
	}

	@Override
	protected void init() {
		labels.clear();
		sliders.clear();
		stateButtons.clear();
		stateSignature = "";
		stateEntityUuid = "";
		listScroll = 0;
		tick = 0;

		boolean compact = height < 440;
		rowHeight = compact ? 22 : 24;
		int leftTop = compact ? 36 : 50;

		button("Toggle Glowing", 10, 10, 100, press -> PivotClient.glowing = !PivotClient.glowing);

		button("Next", width - 125, 10, 110, press -> switchEntity(1));
		button("Previous", width - 125, 32, 110, press -> switchEntity(-1));
		button("Closest", width - 125, 54, 110, press -> selectClosest());

		int y = leftTop;
		rotXSlider = sliderRow("Rotation X", y, -180, 180, 0, "0", 45, "45", this::updateBlockRotationFromEuler);
		y += rowHeight;
		rotYSlider = sliderRow("Rotation Y", y, -180, 180, 0, "0", 45, "45", this::updateBlockRotationFromEuler);
		y += rowHeight;
		rotZSlider = sliderRow("Rotation Z", y, -180, 180, 0, "0", 45, "45", this::updateBlockRotationFromEuler);
		y += rowHeight;
		scaleXSlider = sliderRow("Scale X", y, 0, 10, 1, "1", 0.5, "0.5", this::updateScale);
		y += rowHeight;
		scaleYSlider = sliderRow("Scale Y", y, 0, 10, 1, "1", 0.5, "0.5", this::updateScale);
		y += rowHeight;
		scaleZSlider = sliderRow("Scale Z", y, 0, 10, 1, "1", 0.5, "0.5", this::updateScale);
		y += rowHeight;
		offsetRow("Offset X", 0, y);
		y += rowHeight;
		offsetRow("Offset Y", 1, y);
		y += rowHeight;
		offsetRow("Offset Z", 2, y);

		int rightX = width - 135;
		int alignY = compact ? 80 : 150;
		button("Align X", rightX, alignY, 80, press -> alignCoord("x"));
		button("Align Y", rightX, alignY + 22, 80, press -> alignCoord("y"));
		button("Align Z", rightX, alignY + 44, 80, press -> alignCoord("z"));

		int entityY = compact ? alignY + 3 * 22 + 6 : 225;
		button("New Block Display Entity", rightX, entityY, 130, press -> newBlockdisplayEntity());
		button("New Item Display Entity", rightX, entityY + 22, 130, press -> newItemdisplayEntity());
		button("Remove Display Entity", rightX, entityY + 44, 130, press -> removeDisplayEntity());
		button("Duplicate Display Entity", rightX, entityY + 66, 130, press -> duplicateDisplayEntity());
		lockButton = button("Lock Display Entity", rightX, entityY + 88, 130, press -> toggleLockDisplayEntity());

		listX = width - 225;
		listY = entityY + 5 * 22 + 5;
		listHeight = Mth.clamp(height - listY - 6, MIN_LIST_HEIGHT, MAX_LIST_HEIGHT);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(listX, listY, listX + LIST_WIDTH, listY + listHeight, 0xC0101010);
		graphics.fill(listX, listY, listX + LIST_WIDTH, listY + 1, 0xFF555555);
		graphics.fill(listX, listY + listHeight - 1, listX + LIST_WIDTH, listY + listHeight, 0xFF555555);
		graphics.fill(listX, listY, listX + 1, listY + listHeight, 0xFF555555);
		graphics.fill(listX + LIST_WIDTH - 1, listY, listX + LIST_WIDTH, listY + listHeight, 0xFF555555);

		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		for (Label label : labels) {
			graphics.text(font, label.text(), label.x(), label.y(), 0xFFFFFFFF);
		}

		if (listMaxScroll > 0) {
			int viewHeight = listHeight - LIST_PADDING * 2;
			int thumbHeight = Math.max(12, viewHeight * viewHeight / (viewHeight + listMaxScroll));
			int thumbY = listY + LIST_PADDING + (viewHeight - thumbHeight) * listScroll / listMaxScroll;
			int barX = listX + LIST_WIDTH - 8;
			graphics.fill(barX, listY + LIST_PADDING, barX + 4, listY + listHeight - LIST_PADDING, 0xFF202020);
			graphics.fill(barX, thumbY, barX + 4, thumbY + thumbHeight, 0xFFA0A0A0);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (mouseX >= listX && mouseX < listX + LIST_WIDTH && mouseY >= listY && mouseY < listY + listHeight) {
			listScroll -= (int) (scrollY * 11);
			layoutStateList();
			return true;
		}

		for (ValueSlider slider : sliders) {
			if (slider.isMouseOver(mouseX, mouseY)) {
				slider.nudge(scrollY * slider.scrollStep());
				return true;
			}
		}

		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	private void layoutStateList() {
		int viewTop = listY + LIST_PADDING;
		int viewBottom = listY + listHeight - LIST_PADDING;
		listMaxScroll = Math.max(0, stateButtons.size() * ENTRY_HEIGHT - (viewBottom - viewTop));
		listScroll = Mth.clamp(listScroll, 0, listMaxScroll);

		for (int i = 0; i < stateButtons.size(); i++) {
			Button entry = stateButtons.get(i);
			int entryY = viewTop + i * ENTRY_HEIGHT - listScroll;
			entry.setY(entryY);
			entry.visible = entryY >= viewTop && entryY + 20 <= viewBottom;
		}
	}

	private void refreshStateList(List<StateEntry> entries) {
		String uuid = displayEntity == null ? "" : displayEntity.getStringUUID();
		StringBuilder signature = new StringBuilder(uuid);
		for (StateEntry entry : entries) {
			signature.append('|').append(entry.label());
		}

		if (signature.toString().equals(stateSignature)) return;

		for (Button old : stateButtons) {
			removeWidget(old);
		}
		stateButtons.clear();

		if (!uuid.equals(stateEntityUuid)) {
			listScroll = 0;
		}
		stateEntityUuid = uuid;
		stateSignature = signature.toString();

		for (StateEntry entry : entries) {
			Button entryButton = addRenderableWidget(
					Button.builder(Component.literal(entry.label()), press -> entry.action().run())
							.bounds(listX + LIST_PADDING, listY, ENTRY_WIDTH, 20)
							.build()
			);
			stateButtons.add(entryButton);
		}

		layoutStateList();
	}

	private StateEntry changeState(String label, String id) {
		return new StateEntry(label, () -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getStringUUID(), id)));
	}

	private List<StateEntry> collectStateEntries() {
		List<StateEntry> entries = new ArrayList<>();

		if (displayEntity instanceof Display.BlockDisplay blockDisplayEntity) {
			BlockState state = blockDisplayEntity.getBlockState();

			if (state.is(Blocks.PISTON)) {
				entries.add(changeState("Set Piston Head", "pistonhead"));
				entries.add(changeState("Make Sticky", "makesticky"));
			}
			if (state.is(Blocks.PISTON_HEAD)) {
				entries.add(changeState("Set Piston", "piston"));
			}
			if (state.is(Blocks.STICKY_PISTON)) {
				entries.add(changeState("Set Non Sticky", "nonsticky"));
			}
			if (state.is(Blocks.DIRT)) {
				entries.add(changeState("Set Farmland", "farmland"));
				entries.add(changeState("Set Grass", "grass"));
			}
			if (state.is(Blocks.FARMLAND)) {
				entries.add(changeState("Set Dirt", "dirt"));
				entries.add(changeState("Set Grass", "grass"));
			}
			if (state.is(Blocks.GRASS_BLOCK)) {
				entries.add(changeState("Set Farmland", "farmland"));
				entries.add(changeState("Set Dirt", "dirt"));
			}
			if (state.is(Blocks.BAMBOO)) {
				entries.add(changeState("Set Bamboo Shoot", "bambooshoot"));
			}
			if (state.is(Blocks.BAMBOO_SAPLING)) {
				entries.add(changeState("Set Bamboo", "bambooshoot"));
			}

			for (Property<?> property : state.getProperties()) {
				entries.add(new StateEntry(
						property.getName() + " = " + state.getValue(property),
						() -> handleBlockStateClick(property, blockDisplayEntity.getBlockState())
				));
			}
		}

		if (displayEntity instanceof Display.ItemDisplay) {
			entries.add(new StateEntry("Display = " + getName(currentContext), () -> {
				currentContext = getNextDisplayContext(currentContext);

				ClientPlayNetworking.send(
						new SetItemDisplayContextPacket(
								displayEntity.getStringUUID(),
								currentContext.getSerializedName()
						)
				);

				tick = 0;
			}));
		}

		return entries;
	}

	private <T extends Comparable<T>> void handleBlockStateClick(Property<T> property, BlockState state) {
		if (displayEntity instanceof Display.BlockDisplay blockDisplayEntity) {
			T current = state.getValue(property);
			T next = getNextValue(property, current);

			ClientPlayNetworking.send(
					new SetBlockDisplayBlockstate(
							blockDisplayEntity.getStringUUID(),
							property.getName(),
							property.getName(next)
					)
			);

			tick = 0;
		}
	}

	private static <T extends Comparable<T>> T getNextValue(Property<T> property, T current) {
		ImmutableList<T> values = ImmutableList.copyOf(property.getPossibleValues());
		int idx = values.indexOf(current);
		return values.get((idx + 1) % values.size());
	}

	private void newItemdisplayEntity() {
		ClientPlayNetworking.send(new NewItemDisplayEntity(""));
	}

	private void newBlockdisplayEntity() {
		ClientPlayNetworking.send(new NewBlockDisplayEntity(""));
	}

	private void toggleLockDisplayEntity() {
		if (displayEntity == null) return;

		ClientPlayNetworking.send(new LockDisplayEntity(displayEntity.getStringUUID()));
	}

	private void duplicateDisplayEntity() {
		if (displayEntity == null) return;

		ClientPlayNetworking.send(new DuplicateBlockDisplay(displayEntity.getStringUUID()));
	}

	private void removeDisplayEntity() {
		if (displayEntity == null) return;

		ClientPlayNetworking.send(new TakebackBlockEntity(displayEntity.getStringUUID()));
	}

	private void sendPositionUpdate(double x, double y, double z) {
		if (displayEntity == null) return;

		ClientPlayNetworking.send(new UpdateBlockDisplayPos(displayEntity.getStringUUID(), new Vec3(x, y, z)));
	}

	private void shiftPosition(int axis, double delta) {
		if (displayEntity == null) return;

		double[] position = {displayEntity.getX(), displayEntity.getY(), displayEntity.getZ()};
		position[axis] += delta;
		sendPositionUpdate(position[0], position[1], position[2]);
	}

	private void alignCoord(String coord) {
		if (displayEntity == null) return;

		AlignXYZ alignXYZ = new AlignXYZ(displayEntity.getStringUUID(), coord);

		Scheduler.runLater(() -> tick = 0, 2);

		ClientPlayNetworking.send(alignXYZ);
	}

	private void updateScale() {
		if (displayEntity == null) return;

		sendScaleUpdate(new Vector3f(
				(float) scaleXSlider.getRealValue(),
				(float) scaleYSlider.getRealValue(),
				(float) scaleZSlider.getRealValue()
		));
	}

	private void updateBlockRotationFromEuler() {
		if (displayEntity == null) return;

		sendRotationUpdate(eulerDegreesToQuaternion(
				(float) rotXSlider.getRealValue(),
				(float) rotYSlider.getRealValue(),
				(float) rotZSlider.getRealValue()
		));
	}

	private void switchEntity(int direction) {
		if (PivotClient.DisplayEntitiesInWorld.isEmpty()) return;
		PivotClient.currentIndex = (PivotClient.currentIndex + direction + PivotClient.DisplayEntitiesInWorld.size()) % PivotClient.DisplayEntitiesInWorld.size();
		tick = 0;
	}

	public void setTick() {
		tick = 0;
	}

	private void selectClosest() {
		String closestUuid = null;
		double closestDistance = Double.MAX_VALUE;
		LocalPlayer player = Minecraft.getInstance().player;

		if (player == null || Minecraft.getInstance().level == null) return;

		for (UUID uuid : PivotClient.DisplayEntitiesInWorld) {
			Display display = (Display) Minecraft.getInstance().level.getEntity(uuid);
			if (display != null) {
				double distance = display.distanceTo(player);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestUuid = display.getStringUUID();
				}
			}
		}

		String finalUuid = closestUuid;

		Scheduler.runLater(() -> {
			if (finalUuid != null) {
				UUID selectedBlockDisplayEntity = UUID.fromString(finalUuid);
				int i = PivotClient.DisplayEntitiesInWorld.indexOf(selectedBlockDisplayEntity);
				if (i != -1) {
					PivotClient.currentIndex = i;
				}
			}
		}, 2);
	}

	private void sendRotationUpdate(Quaternionf q) {
		if (displayEntity == null) return;

		ClientPlayNetworking.send(new UpdateBlockDisplayPacketC2S(displayEntity.getUUID().toString(), null, q, null));
	}

	private void sendScaleUpdate(Vector3f q) {
		if (displayEntity == null) return;

		ClientPlayNetworking.send(new UpdateBlockDisplayPacketC2S(displayEntity.getUUID().toString(), null, null, q));
	}

	@Override
	public void tick() {
		super.tick();

		if (displayEntity != null && Util.getLockedDisplayEntity(displayEntity)) {
			lockButton.setMessage(Component.literal("Unlock Display Entity"));
		} else {
			lockButton.setMessage(Component.literal("Lock Display Entity"));
		}

		if (!PivotClient.DisplayEntitiesInWorld.isEmpty()) {
			if (PivotClient.currentIndex >= PivotClient.DisplayEntitiesInWorld.size()) {
				PivotClient.currentIndex = 0;
			}
			displayEntity = PivotClient.getDisplayEntity(PivotClient.DisplayEntitiesInWorld.get(PivotClient.currentIndex));
		} else {
			displayEntity = null;
		}

		refreshStateList(collectStateEntries());

		if (tick == 0 && displayEntity != null) {
			Transformation transformation = Display.createTransformation(displayEntity.getEntityData());
			Quaternionf rightRot = new Quaternionf(transformation.rightRotation());
			Vector3f scale = new Vector3f(transformation.scale());
			float[] eulers = quaternionToEulerDegrees(rightRot);

			rotXSlider.setRealValue(eulers[0], false);
			rotYSlider.setRealValue(eulers[1], false);
			rotZSlider.setRealValue(eulers[2], false);

			scaleXSlider.setRealValue(scale.x, false);
			scaleYSlider.setRealValue(scale.y, false);
			scaleZSlider.setRealValue(scale.z, false);
		}
		tick++;
	}

	private String getName(ItemDisplayContext context) {
		switch (context.getSerializedName().toLowerCase()) {
			case "thirdperson_lefthand":
				return "3rd person lefthand";
			case "thirdperson_righthand":
				return "3rd person righthand";
			case "firstperson_lefthand":
				return "1st person lefthand";
			case "firstperson_righthand":
				return "1st person righthand";
			default:
				return context.getSerializedName();
		}
	}

	private ItemDisplayContext getNextDisplayContext(ItemDisplayContext current) {
		ItemDisplayContext[] values = ItemDisplayContext.values();
		int nextIndex = (current.ordinal() + 1) % values.length;
		return values[nextIndex];
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
