package bikerboys.pivot.screen;

import bikerboys.pivot.Pivot;
import bikerboys.pivot.PivotClient;
import bikerboys.pivot.Scheduler;
import bikerboys.pivot.networking.packets.*;
import bikerboys.pivot.util.Util;
import com.google.common.collect.ImmutableList;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import static bikerboys.pivot.util.Util.eulerDegreesToQuaternion;
import static bikerboys.pivot.util.Util.quaternionToEulerDegrees;

public class BlockDisplayEntityEditScreen extends BaseOwoScreen<FlowLayout> {
    private ItemDisplayContext currentContext = ItemDisplayContext.NONE;
    private int tick = 0;
    private boolean suppressUpdates = false;
    private DisplayEntity displayEntity;

    private ScrollContainer<Component> blockStateScroll = null;
    private FlowLayout blockStateFlow = null;

    // Euler sliders
    private final DiscreteSliderComponent rotXSlider = Components.discreteSlider(Sizing.fixed(140), -180f, 180f).decimalPlaces(2);
    private final DiscreteSliderComponent rotYSlider = Components.discreteSlider(Sizing.fixed(140), -180f, 180f).decimalPlaces(2);
    private final DiscreteSliderComponent rotZSlider = Components.discreteSlider(Sizing.fixed(140), -180f, 180f).decimalPlaces(2);

    private final DiscreteSliderComponent scaleXSlider = Components.discreteSlider(Sizing.fixed(140), 0f, 10f).decimalPlaces(2);
    private final DiscreteSliderComponent scaleYSlider = Components.discreteSlider(Sizing.fixed(140), 0f, 10f).decimalPlaces(2);
    private final DiscreteSliderComponent scaleZSlider = Components.discreteSlider(Sizing.fixed(140), 0f, 10f).decimalPlaces(2);

    private final TextBoxComponent positionXbox = Components.textBox(Sizing.fixed(140));
    private final TextBoxComponent positionYbox = Components.textBox(Sizing.fixed(140));
    private final TextBoxComponent positionZbox = Components.textBox(Sizing.fixed(140));

    private final ButtonComponent zeroX = Components.button(Text.literal("0"), (press -> rotXSlider.setFromDiscreteValue(0)));
    private final ButtonComponent zeroY = Components.button(Text.literal("0"), (press -> rotYSlider.setFromDiscreteValue(0)));
    private final ButtonComponent zeroZ = Components.button(Text.literal("0"), (press -> rotZSlider.setFromDiscreteValue(0)));

    private final DiscreteSliderComponent transXSlider = Components.discreteSlider(Sizing.fixed(140), -2.0, 2.0).decimalPlaces(2);
    private final DiscreteSliderComponent transYSlider = Components.discreteSlider(Sizing.fixed(140), -2.0, 2.0).decimalPlaces(2);
    private final DiscreteSliderComponent transZSlider = Components.discreteSlider(Sizing.fixed(140), -2.0, 2.0).decimalPlaces(2);



    ButtonComponent tZeroX = Components.button(Text.literal("0"), press -> transXSlider.setFromDiscreteValue(0.0));
    ButtonComponent tZeroY = Components.button(Text.literal("0"), press -> transYSlider.setFromDiscreteValue(0.0));
    ButtonComponent tZeroZ = Components.button(Text.literal("0"), press -> transZSlider.setFromDiscreteValue(0.0));

    // Navigation
    private final ButtonComponent nextEntity = Components.button(Text.literal("Next"), btn -> switchEntity(1));
    private final ButtonComponent previousEntity = Components.button(Text.literal("Previous"), btn -> switchEntity(-1));
    private final ButtonComponent closestEntity = Components.button(Text.literal("Closest"), btn -> selectClosest());



    // Buttons
    private final ButtonComponent advancedModeButton = Components.button(Text.literal("Advanced Mode"), btn -> PivotClient.advancedmode = !PivotClient.advancedmode);
    private final ButtonComponent toggleGlowButton = Components.button(Text.literal("Toggle Glowing"), btn -> PivotClient.glowing = !PivotClient.glowing);

    private final ButtonComponent lockButton = Components.button(Text.literal(""), press -> toggleLockDisplayEntity());

    public BlockDisplayEntityEditScreen() {
        super(Text.literal(""));
    }





    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }


    @Override
    protected void build(FlowLayout root) {
        // --- Next / Previous (top-right) ---
        FlowLayout buttons = Containers.verticalFlow(Sizing.content(), Sizing.content());
        buttons.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        buttons.positioning(Positioning.absolute(this.width - 125, 10));
        buttons.child(nextEntity.horizontalSizing(Sizing.fixed(110)));
        buttons.child(previousEntity.horizontalSizing(Sizing.fixed(110)));
        buttons.child(closestEntity.horizontalSizing(Sizing.fixed(110)));

        // --- Rotation Sliders ---
        FlowLayout rotationSliders = Containers.verticalFlow(Sizing.content(), Sizing.content());
        rotationSliders.alignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP);

        Function<DiscreteSliderComponent, FlowLayout> rotationRow = (slider) -> {
            FlowLayout row = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(24));
            row.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
            row.child(slider.horizontalSizing(Sizing.fixed(140)));
            row.child(Components.button(Text.literal("0"), btn -> slider.setFromDiscreteValue(0)).horizontalSizing(Sizing.fixed(20)));
            row.child(Components.button(Text.literal("-45"), btn -> slider.setFromDiscreteValue(slider.discreteValue() - 45)));
            row.child(Components.button(Text.literal("+45"), btn -> slider.setFromDiscreteValue(slider.discreteValue() + 45)));
            return row;
        };

        rotXSlider.scrollStep(0.00003);
        rotYSlider.scrollStep(0.00003);
        rotZSlider.scrollStep(0.00003);

        scaleXSlider.scrollStep(0.001);
        scaleYSlider.scrollStep(0.001);
        scaleZSlider.scrollStep(0.001);

        transXSlider.scrollStep(0.003);
        transYSlider.scrollStep(0.003);
        transZSlider.scrollStep(0.003);

        rotationSliders.child(Containers.horizontalFlow(Sizing.content(), Sizing.fixed(24))
                .child(Components.label(Text.literal("Rotation X")).horizontalSizing(Sizing.fixed(70)))
                .child(rotationRow.apply(rotXSlider))
        );
        rotationSliders.child(Containers.horizontalFlow(Sizing.content(), Sizing.fixed(24))
                .child(Components.label(Text.literal("Rotation Y")).horizontalSizing(Sizing.fixed(70)))
                .child(rotationRow.apply(rotYSlider))
        );
        rotationSliders.child(Containers.horizontalFlow(Sizing.content(), Sizing.fixed(24))
                .child(Components.label(Text.literal("Rotation Z")).horizontalSizing(Sizing.fixed(70)))
                .child(rotationRow.apply(rotZSlider))
        );

        // --- Scale sliders ---
        BiFunction<String, DiscreteSliderComponent, FlowLayout> scaleRow = (label, slider) -> {
            FlowLayout row = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(24));
            row.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
            row.child(Components.label(Text.literal(label)).horizontalSizing(Sizing.fixed(70)));
            row.child(slider.horizontalSizing(Sizing.fixed(140)));
            row.child(Components.button(Text.literal("1"), btn -> slider.setFromDiscreteValue(1.0)).horizontalSizing(Sizing.fixed(20)));
            row.child(Components.button(Text.literal("-0.5"), btn -> slider.setFromDiscreteValue(slider.discreteValue() - 0.5)));
            row.child(Components.button(Text.literal("+0.5"), btn -> slider.setFromDiscreteValue(slider.discreteValue() + 0.5)));
            return row;
        };

        rotationSliders.child(scaleRow.apply("Scale X", scaleXSlider));
        rotationSliders.child(scaleRow.apply("Scale Y", scaleYSlider));
        rotationSliders.child(scaleRow.apply("Scale Z", scaleZSlider));

        // --- Translation sliders ---
        BiFunction<String, DiscreteSliderComponent, FlowLayout> transRow = (label, slider) -> {
            FlowLayout row = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(24));
            row.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
            row.child(Components.label(Text.literal(label)).horizontalSizing(Sizing.fixed(70)));
            row.child(slider.horizontalSizing(Sizing.fixed(140)));
            row.child(Components.button(Text.literal("0"), btn -> slider.setFromDiscreteValue(0.0)).horizontalSizing(Sizing.fixed(20)));
            row.child(Components.button(Text.literal("-0.05"), btn -> slider.setFromDiscreteValue(slider.discreteValue() - 0.05)));
            row.child(Components.button(Text.literal("+0.05"), btn -> slider.setFromDiscreteValue(slider.discreteValue() + 0.05)));
            return row;
        };

        rotationSliders.child(transRow.apply("Translation X", transXSlider));
        rotationSliders.child(transRow.apply("Translation Y", transYSlider));
        rotationSliders.child(transRow.apply("Translation Z", transZSlider));

        // --- Left column wrapper ---
        FlowLayout leftColumn = Containers.verticalFlow(Sizing.content(), Sizing.content());
        leftColumn.alignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
        leftColumn.positioning(Positioning.absolute(10, 50));
        leftColumn.child(rotationSliders);

        // --- Advanced Mode button ---
        FlowLayout advWrap = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        advWrap.positioning(Positioning.absolute(10, 10));
        advWrap.gap(5);
       // advWrap.child(advancedModeButton);
        advWrap.child(toggleGlowButton);



        FlowLayout newEntityWrapper = Containers.verticalFlow(Sizing.content(), Sizing.content());
        newEntityWrapper.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);

        newEntityWrapper.child(
                Components.button(Text.literal("New Block Display Entity"), press -> newBlockdisplayEntity())
                        .horizontalSizing(Sizing.fixed(130))
        );

        newEntityWrapper.child(
                Components.button(Text.literal("New Item Display Entity"), press -> newItemdisplayEntity())
                        .horizontalSizing(Sizing.fixed(130))
        );

        newEntityWrapper.positioning(Positioning.absolute(this.width - 125 - 10, 230));




        newEntityWrapper.child(
                Components.button(Text.literal("Remove Display Entity"), press -> removeDisplayEntity())
                        .horizontalSizing(Sizing.fixed(130))
        );

        newEntityWrapper.child(
                Components.button(Text.literal("Duplicate Display Entity"), press -> duplicateDisplayEntity())
                        .horizontalSizing(Sizing.fixed(130))
        );



        newEntityWrapper.child(lockButton.horizontalSizing(Sizing.fixed(130)));

        newEntityWrapper.positioning(Positioning.absolute(this.width - 125 - 10, 225));




        FlowLayout posWrapper = Containers.verticalFlow(Sizing.content(), Sizing.content());
        posWrapper.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);


        FlowLayout applyRow = Containers.horizontalFlow(Sizing.fixed(200), Sizing.content());
        applyRow.alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);

        applyRow.child(
                Components.button(Text.literal("Apply Position"), btn -> {
                    if (displayEntity != null) {
                        try {
                            double newX = Double.parseDouble(positionXbox.getText());
                            double newY = Double.parseDouble(positionYbox.getText());
                            double newZ = Double.parseDouble(positionZbox.getText());
                            sendPositionUpdate(newX, newY, newZ);
                        } catch (NumberFormatException ignored) {}
                    }
                }).horizontalSizing(Sizing.fixed(129))
        );


// add above the X/Y/Z rows
        posWrapper.child(applyRow);

        // X row
        // X row
        FlowLayout xRow = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(24));
        xRow.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        xRow.child(Components.label(Text.literal("Position X")).horizontalSizing(Sizing.fixed(70)));
        positionXbox.setMaxLength(12);
        positionXbox.setTextPredicate(s -> s.matches("-?\\d*(\\.\\d+)?"));
        xRow.child(positionXbox);
// -1 / +1 buttons
        xRow.child(Components.button(Text.literal("-1"), btn -> {
            if (displayEntity != null) {
                double newX = displayEntity.getX() - 1;
                sendPositionUpdate(newX, displayEntity.getY(), displayEntity.getZ());
                positionXbox.setText(String.valueOf(newX));
            }
        }).horizontalSizing(Sizing.fixed(30)));
        xRow.child(Components.button(Text.literal("+1"), btn -> {
            if (displayEntity != null) {
                double newX = displayEntity.getX() + 1;
                sendPositionUpdate(newX, displayEntity.getY(), displayEntity.getZ());
                positionXbox.setText(String.valueOf(newX));
            }
        }).horizontalSizing(Sizing.fixed(30)));
        posWrapper.child(xRow);

        // Y row
        FlowLayout yRow = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(24));
        yRow.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        yRow.child(Components.label(Text.literal("Position Y")).horizontalSizing(Sizing.fixed(70)));
        positionYbox.setMaxLength(12);
        positionYbox.setTextPredicate(s -> s.matches("-?\\d*(\\.\\d+)?"));
        yRow.child(positionYbox);
// -1 / +1 buttons
        yRow.child(Components.button(Text.literal("-1"), btn -> {
            if (displayEntity != null) {
                double newY = displayEntity.getY() - 1;
                sendPositionUpdate( displayEntity.getX(), newY, displayEntity.getZ());
                positionYbox.setText(String.valueOf(newY));
            }
        }).horizontalSizing(Sizing.fixed(30)));
        yRow.child(Components.button(Text.literal("+1"), btn -> {
            if (displayEntity != null) {
                double newY = displayEntity.getY() + 1;
                sendPositionUpdate( displayEntity.getX(), newY, displayEntity.getZ());
                positionYbox.setText(String.valueOf(newY));
            }
        }).horizontalSizing(Sizing.fixed(30)));
        posWrapper.child(yRow);

        FlowLayout zRow = Containers.horizontalFlow(Sizing.content(), Sizing.fixed(24));
        zRow.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        zRow.child(Components.label(Text.literal("Position Z")).horizontalSizing(Sizing.fixed(70)));
        positionZbox.setMaxLength(12);
        positionZbox.setTextPredicate(s -> s.matches("-?\\d*(\\.\\d+)?"));
        zRow.child(positionZbox);
// -1 / +1 buttons
        zRow.child(Components.button(Text.literal("-1"), btn -> {
            if (displayEntity != null) {
                double newZ = displayEntity.getZ() - 1;
                sendPositionUpdate( displayEntity.getX(), displayEntity.getY(),  newZ);
                positionZbox.setText(String.valueOf(newZ));
            }
        }).horizontalSizing(Sizing.fixed(30)));
        zRow.child(Components.button(Text.literal("+1"), btn -> {
            if (displayEntity != null) {
                double newZ = displayEntity.getZ() + 1;
                sendPositionUpdate( displayEntity.getX(),  displayEntity.getY(),  newZ);
                positionZbox.setText(String.valueOf(newZ));
            }
        }).horizontalSizing(Sizing.fixed(30)));
        posWrapper.child(zRow);


        /*
        // hook text box updates
        positionXbox.onChanged().subscribe(text -> {
            if (displayEntity != null && !text.isEmpty()) {
                try {
                    double val = Double.parseDouble(text);
                    sendPositionUpdate( val, displayEntity.getY(), displayEntity.getZ());
                } catch (NumberFormatException ignored) {}
            }
        });

        positionYbox.onChanged().subscribe(text -> {
            if (displayEntity != null && !text.isEmpty()) {
                try {
                    float val = Float.parseFloat(text);
                    sendPositionUpdate( displayEntity.getX(), val, displayEntity.getZ());
                } catch (NumberFormatException ignored) {}
            }
        });

        positionZbox.onChanged().subscribe(text -> {
            if (displayEntity != null && !text.isEmpty()) {
                try {
                    double val = Double.parseDouble(text);
                    sendPositionUpdate( displayEntity.getX(), displayEntity.getY(), val);
                } catch (NumberFormatException ignored) {}
            }
        });
         */

        blockStateFlow = Containers.verticalFlow(Sizing.content(), Sizing.content());// optional spacing




        blockStateScroll = Containers.verticalScroll(
                        Sizing.fixed(220), // width of scroll area
                        Sizing.fixed(130), // height of scroll area
                        blockStateFlow
                );

                blockStateScroll.scrollbarThiccness(20)
                .scrollbar(ScrollContainer.Scrollbar.vanilla())
                .padding(Insets.of(5))
                .surface(Surface.DARK_PANEL);







// Position in your UI
        blockStateScroll.positioning(Positioning.absolute(this.width - 225, 340));

// Add to root
        root.child(blockStateScroll);



        // position block on screen
        posWrapper.positioning(Positioning.absolute(10, 387));
        root.child(posWrapper);





        FlowLayout dropdownWrapper = Containers.verticalFlow(Sizing.content(), Sizing.content());
        dropdownWrapper.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);

        dropdownWrapper.child(Components.button(Text.literal("Align X"), press -> alignCoord("x")).horizontalSizing(Sizing.fixed(80)));
        dropdownWrapper.child(Components.button(Text.literal("Align Y"), press -> alignCoord("y")).horizontalSizing(Sizing.fixed(80)));
        dropdownWrapper.child(Components.button(Text.literal("Align Z"), press -> alignCoord("z")).horizontalSizing(Sizing.fixed(80)));

        dropdownWrapper.positioning(Positioning.absolute(this.width - 125 - 10, 150));

        root.child(dropdownWrapper);



        // --- Add everything to root ---
        root.child(advWrap);
        root.child(buttons);
        root.child(leftColumn);

        root.child(newEntityWrapper);



        FlowLayout axisControls = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        axisControls.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);


    }

    private <T extends Comparable<T>> void handleBlockStateClick(Property<T> property, BlockState state) {
        if (displayEntity instanceof DisplayEntity.BlockDisplayEntity blockDisplayEntity) {
            T current = state.get(property);
            T next = getNextValue(property, current);


            ClientPlayNetworking.send(
                    new SetBlockDisplayBlockstate(
                            blockDisplayEntity.getUuidAsString(),
                            property.getName(),
                            property.name(next) // ✅ correct serialization
                    )
            );

            tick = 0;

        }
    }

    // cycles through property values
    private static <T extends Comparable<T>> T getNextValue(Property<T> property, T current) {
        ImmutableList<T> values = ImmutableList.copyOf(property.getValues());
        int idx = values.indexOf(current);
        return values.get((idx + 1) % values.size());
    }


    private void newItemdisplayEntity() {
        NewItemDisplayEntity NewItemDisplayEntity = new NewItemDisplayEntity("");
        ClientPlayNetworking.send(NewItemDisplayEntity);
    }


    private void toggleLockDisplayEntity() {
        if  (displayEntity == null) return;

        LockDisplayEntity LockDisplayEntity = new LockDisplayEntity(displayEntity.getUuidAsString());

        ClientPlayNetworking.send(LockDisplayEntity);






    }

    private void duplicateDisplayEntity() {
        if  (displayEntity == null) return;

        DuplicateBlockDisplay duplicateBlockDisplay = new DuplicateBlockDisplay(displayEntity.getUuidAsString());

        ClientPlayNetworking.send(duplicateBlockDisplay);







    }


    private void sendPositionUpdate(double x, double y, double z) {
        if (displayEntity == null) return;

        UpdateBlockDisplayPos updateBlockDisplayPos = new UpdateBlockDisplayPos(displayEntity.getUuidAsString(), new Vec3d(x, y, z));


        ClientPlayNetworking.send(updateBlockDisplayPos);
    }

    public void removeDisplayEntity() {
        if (displayEntity == null) return;

        TakebackBlockEntity TakebackBlockEntity = new TakebackBlockEntity(displayEntity.getUuidAsString());
        ClientPlayNetworking.send(TakebackBlockEntity);
    }

    public void newBlockdisplayEntity() {

        NewBlockDisplayEntity newBlockDisplayEntity = new NewBlockDisplayEntity("");
        ClientPlayNetworking.send(newBlockDisplayEntity);






    }


    public void alignCoord(String coord) {
        if (displayEntity == null) return;
        positionXbox.setText(String.valueOf(Float.valueOf((float) displayEntity.getX())));
        positionYbox.setText(String.valueOf(Float.valueOf((float) displayEntity.getY())));
        positionZbox.setText(String.valueOf(Float.valueOf((float) displayEntity.getZ())));


        AlignXYZ alignXYZ = new AlignXYZ(displayEntity.getUuidAsString(), coord);


        Scheduler.runLater(() -> {tick = 0;}, 2);

        ClientPlayNetworking.send(alignXYZ);

    }


    @Override
    protected void init() {
        super.init();

        // Euler slider updates
        rotXSlider.onChanged().subscribe(v -> { if (!suppressUpdates) updateBlockRotationFromEuler(); });
        rotYSlider.onChanged().subscribe(v -> { if (!suppressUpdates) updateBlockRotationFromEuler(); });
        rotZSlider.onChanged().subscribe(v -> { if (!suppressUpdates) updateBlockRotationFromEuler(); });

        scaleXSlider.onChanged().subscribe(v -> { if (!suppressUpdates) updateScale(); });
        scaleYSlider.onChanged().subscribe(v -> { if (!suppressUpdates) updateScale(); });
        scaleZSlider.onChanged().subscribe(v -> { if (!suppressUpdates) updateScale(); });


        transXSlider.onChanged().subscribe(v -> { if (!suppressUpdates) updatetransformation(); });
        transYSlider.onChanged().subscribe(v -> { if (!suppressUpdates) updatetransformation(); });
        transZSlider.onChanged().subscribe(v -> { if (!suppressUpdates) updatetransformation(); });







    }


    private void updateScale() {
        if (displayEntity == null) return;

        Vector3f q = new Vector3f(
                (float) scaleXSlider.discreteValue(),
                (float) scaleYSlider.discreteValue(),
                (float) scaleZSlider.discreteValue()
        );

        sendScaleUpdate(q);
        suppressUpdates = true;

        scaleXSlider.setFromDiscreteValue(q.x);
        scaleYSlider.setFromDiscreteValue(q.y);
        scaleZSlider.setFromDiscreteValue(q.z);
        suppressUpdates = false;
    }

    private void updatetransformation() {
        if (displayEntity == null) return;

        Vector3f q = new Vector3f(
                (float) transXSlider.discreteValue(),
                (float) transYSlider.discreteValue(),
                (float) transZSlider.discreteValue()
        );

        sendTransUpdate(q);
        suppressUpdates = true;

        transXSlider.setFromDiscreteValue(q.x);
        transYSlider.setFromDiscreteValue(q.y);
        transZSlider.setFromDiscreteValue(q.z);
        suppressUpdates = false;
    }





    private void switchEntity(int direction) {
        if (PivotClient.DisplayEntitiesInWorld.isEmpty()) return;
        PivotClient.currentIndex = (PivotClient.currentIndex + direction + PivotClient.DisplayEntitiesInWorld.size()) % PivotClient.DisplayEntitiesInWorld.size();
        tick = 0;
    }

    private void updateBlockRotationFromEuler() {
        if (displayEntity == null) return;

        Quaternionf q = eulerDegreesToQuaternion(
                (float) rotXSlider.discreteValue(),
                (float) rotYSlider.discreteValue(),
                (float) rotZSlider.discreteValue()
        );

        sendRotationUpdate(q);


    }

    public void setTick() {
        tick = 0;
    }


    private void selectClosest() {
        String closestUuid = null;
        double closestDistance = Double.MAX_VALUE; // Start with a very large number
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null || MinecraftClient.getInstance().world == null) return;

        for (UUID uuid : PivotClient.DisplayEntitiesInWorld) {
            DisplayEntity displayEntity = (DisplayEntity) MinecraftClient.getInstance().world.getEntity(uuid);
            if (displayEntity != null) {
                double distance = displayEntity.distanceTo(player);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestUuid = displayEntity.getUuidAsString();
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

        ClientPlayNetworking.send(new UpdateBlockDisplayPacketC2S(
                displayEntity.getUuid().toString(),
                null,
                q,
                null
        ));
    }

    private void sendScaleUpdate(Vector3f q) {
        if (displayEntity == null) return;
        ClientPlayNetworking.send(new UpdateBlockDisplayPacketC2S(
                displayEntity.getUuid().toString(),
                null,
                null,
                q
        ));
    }

    private void sendTransUpdate(Vector3f q) {
        if (displayEntity == null) return;
        ClientPlayNetworking.send(new UpdateBlockDisplayPacketC2S(
                displayEntity.getUuid().toString(),
                q,
                null,
                null
        ));
    }



    @Override
    public void tick() {
        super.tick();

        if (displayEntity != null) {
            boolean lockedDisplayEntity = Util.getLockedDisplayEntity(displayEntity);
            if (lockedDisplayEntity) {
                lockButton.setMessage(Text.literal("Unlock Display Entity"));
            } else {
                lockButton.setMessage(Text.literal("Lock Display Entity"));
            }
        } else {
            lockButton.setMessage(Text.literal("Lock Display Entity"));
        }

        if (!PivotClient.DisplayEntitiesInWorld.isEmpty()) {
            if (PivotClient.currentIndex >= PivotClient.DisplayEntitiesInWorld.size()) {
                PivotClient.currentIndex = 0;
            }
            displayEntity = PivotClient.getDisplayEntity(PivotClient.DisplayEntitiesInWorld.get(PivotClient.currentIndex));
        } else {
            displayEntity = null;
        }

        if (displayEntity instanceof DisplayEntity.BlockDisplayEntity blockDisplayEntity) {


            blockStateFlow.clearChildren();




            BlockState state = blockDisplayEntity.getBlockState();


            if (state.isOf(Blocks.PISTON)) {
                blockStateFlow.child(Components.button(Text.literal("Set Piston Head"), btn -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getUuidAsString(), "pistonhead"))).horizontalSizing(Sizing.fixed(188)));
                blockStateFlow.child(Components.button(Text.literal("Make Sticky"), btn -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getUuidAsString(), "makesticky"))).horizontalSizing(Sizing.fixed(188)));
            }

            if (state.isOf(Blocks.PISTON_HEAD)) {
                blockStateFlow.child(Components.button(Text.literal("Set Piston"), btn -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getUuidAsString(), "piston"))).horizontalSizing(Sizing.fixed(188)));
            }

            if (state.isOf(Blocks.STICKY_PISTON)) {
                blockStateFlow.child(Components.button(Text.literal("Set Non Sticky"), btn -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getUuidAsString(), "nonsticky"))).horizontalSizing(Sizing.fixed(188)));
            }



            if (state.isOf(Blocks.DIRT)) {
                blockStateFlow.child(Components.button(Text.literal("Set Farmland"), btn -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getUuidAsString(), "farmland"))).horizontalSizing(Sizing.fixed(188)));
                blockStateFlow.child(Components.button(Text.literal("Set Grass"), btn -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getUuidAsString(), "grass"))).horizontalSizing(Sizing.fixed(188)));
            }
            if (state.isOf(Blocks.FARMLAND)) {
                blockStateFlow.child(Components.button(Text.literal("Set Dirt"), btn -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getUuidAsString(), "dirt"))).horizontalSizing(Sizing.fixed(188)));
                blockStateFlow.child(Components.button(Text.literal("Set Grass"), btn -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getUuidAsString(), "grass"))).horizontalSizing(Sizing.fixed(188)));

            }
            if (state.isOf(Blocks.GRASS_BLOCK)) {

                blockStateFlow.child(Components.button(Text.literal("Set Farmland"), btn -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getUuidAsString(), "farmland"))).horizontalSizing(Sizing.fixed(188)));
                blockStateFlow.child(Components.button(Text.literal("Set Dirt"), btn -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getUuidAsString(), "dirt"))).horizontalSizing(Sizing.fixed(188)));
            }
            if (state.isOf(Blocks.BAMBOO)) {
                blockStateFlow.child(Components.button(Text.literal("Set Bamboo Shoot"), btn -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getUuidAsString(), "bambooshoot"))).horizontalSizing(Sizing.fixed(188)));
            }
            if (state.isOf(Blocks.BAMBOO_SAPLING)) {
                blockStateFlow.child(Components.button(Text.literal("Set Bamboo"), btn -> ClientPlayNetworking.send(new ChangeBlockDisplayState(displayEntity.getUuidAsString(), "bambooshoot"))).horizontalSizing(Sizing.fixed(188)));

            }
            for (Property<?> property : state.getProperties()) {
                blockStateFlow.child(Components.button(
                        Text.literal(property.getName() + " = " + state.get(property)),
                        btn -> handleBlockStateClick(property, blockDisplayEntity.getBlockState()) // always fresh state
                ).horizontalSizing(Sizing.fixed(188)));
            }

        }


        if (displayEntity instanceof DisplayEntity.ItemDisplayEntity itemDisplayEntity) {

            blockStateFlow.clearChildren();


            blockStateFlow.child(Components.button(
                    Text.literal("Display = " + getName(currentContext)),
                    btn -> {
                        // cycle to next
                        currentContext = getNextDisplayContext(currentContext);

                        // update button label



                        // send to server (similar to your blockstate handling)
                        ClientPlayNetworking.send(
                                new SetItemDisplayContextPacket(
                                        displayEntity.getUuidAsString(),
                                        currentContext.asString()
                                )
                        );

                        tick = 0;
                    }
            ).horizontalSizing(Sizing.fixed(188)));


        }

        // On first tick after switching entity, load values into sliders
        if (tick == 0 && displayEntity != null) {







            AffineTransformation transformation = DisplayEntity.getTransformation(displayEntity.getDataTracker());
            Quaternionf rightRot = transformation.getRightRotation();
            Vector3f scale = transformation.getScale();
            Vector3f translation = transformation.getTranslation();
            float[] eulers = quaternionToEulerDegrees(rightRot);

            suppressUpdates = true;


            rotXSlider.setFromDiscreteValue(eulers[0]);
            rotYSlider.setFromDiscreteValue(eulers[1]);
            rotZSlider.setFromDiscreteValue(eulers[2]);

            transXSlider.setFromDiscreteValue(translation.x);
            transYSlider.setFromDiscreteValue(translation.y);
            transZSlider.setFromDiscreteValue(translation.z);

            scaleXSlider.setFromDiscreteValue(scale.x);
            scaleYSlider.setFromDiscreteValue(scale.y);
            scaleZSlider.setFromDiscreteValue(scale.z);

            positionXbox.setText(String.valueOf(Float.valueOf((float) displayEntity.getX())));
            positionYbox.setText(String.valueOf(Float.valueOf((float) displayEntity.getY())));
            positionZbox.setText(String.valueOf(Float.valueOf((float) displayEntity.getZ())));

            suppressUpdates = false;
        }
        tick++;


    }

    private String getName(ItemDisplayContext context) {
        switch (context.asString().toLowerCase()) {
            case "thirdperson_lefthand":
                return "3rd person lefthand";
            case "thirdperson_righthand":
                return "3rd person righthand";
            case "firstperson_lefthand":
                return "1st person lefthand";
            case "firstperson_righthand":
                return "1st person righthand";
            default:
                return context.asString(); // fallback
        }
    }


    private ItemDisplayContext getNextDisplayContext(ItemDisplayContext current) {
        ItemDisplayContext[] values = ItemDisplayContext.values();
        int nextIndex = (current.ordinal() + 1) % values.length;
        return values[nextIndex];
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
    }
}