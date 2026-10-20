package bikerboys.pivot.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Locale;

public class ValueSlider extends AbstractSliderButton {
	private final double min;
	private final double max;
	private final Runnable onChange;

	public ValueSlider(int x, int y, int width, double min, double max, Runnable onChange) {
		super(x, y, width, 20, Component.empty(), 0.0);
		this.min = min;
		this.max = max;
		this.onChange = onChange;
		this.value = normalize(0.0);
		updateMessage();
	}

	public double getRealValue() {
		return min + value * (max - min);
	}

	public void setRealValue(double realValue, boolean notify) {
		this.value = normalize(realValue);
		updateMessage();

		if (notify) {
			onChange.run();
		}
	}

	public void nudge(double amount) {
		setRealValue(getRealValue() + amount, true);
	}

	public double scrollStep() {
		return (max - min) / 100.0;
	}

	private double normalize(double realValue) {
		return Mth.clamp((realValue - min) / (max - min), 0.0, 1.0);
	}

	@Override
	protected void updateMessage() {
		setMessage(Component.literal(String.format(Locale.ROOT, "%.2f", getRealValue())));
	}

	@Override
	protected void applyValue() {
		onChange.run();
	}
}
