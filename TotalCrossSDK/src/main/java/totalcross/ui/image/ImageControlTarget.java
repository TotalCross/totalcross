package totalcross.ui.image;

import java.util.ArrayList;
import java.util.List;

import totalcross.ui.ImageControl;
import totalcross.ui.image.ImageLoader.SizeReadyCallback;
import totalcross.ui.image.ImageLoader.Target;

public class ImageControlTarget implements Target<ImageControl> {

	ImageControl ic;
	List<SizeReadyCallback> callbacks;

	ImageControlTarget(ImageControl ic) {
		this.ic = ic;
		ic.target = this;
	}

	@Override
	public void getSize(SizeReadyCallback callback) {
		if (callbacks == null) {
			callbacks = new ArrayList<>();
		}
		callbacks.add(callback);
	}

	public void sizeChanged(int width, int height) {
		if (callbacks != null) {
			for (SizeReadyCallback sizeReadyCallback : callbacks) {
				sizeReadyCallback.onSizeReady(width, height);
			}
		}
	}
}
