package totalcross.ui.image;

import java.io.IOException;
import java.util.HashMap;

import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.ImageControl;
import totalcross.ui.event.SizeChangeEvent;
import totalcross.ui.event.SizeChangeHandler;
import totalcross.util.concurrent.Lock;

public class ImageLoader {

	public static interface ScaleType {
		public static final int CENTER = 0;
		public static final int CENTER_CLIP = 1;
		public static final int CENTER_CROP = 2;
		public static final int CENTER_INSIDE = 3;
		public static final int FIT_CENTER = 4;
		public static final int FIT_START = 5;
		public static final int FIT_END = 6;
		public static final int FIT_XY = 7;
		public static final int FOCUS_CROP = 8;
		public static final int NONE = 9;
	}

	private String path;

	private SimpleImageInfo sif;

	private HashMap<String, Image> mipmap = new HashMap<>();

	private static HashMap<String, ImageLoader> cache = new HashMap<>();

	private static Lock lock = new Lock();

	public static ImageLoader get(String path) throws IOException {
		synchronized (lock) {
			ImageLoader loader = cache.get(path);
			if (loader != null) {
				return loader;
			}
			loader = new ImageLoader(path);
			cache.put(path, loader);
			return loader;
		}
	}

	private ImageLoader(String path) throws java.io.IOException {
		this.path = path;
	}

	public Image getImage(int desiredWidth, int desiredHeight) throws IOException, ImageException {
		final String key = desiredWidth + "x" + desiredHeight;
		Image i = mipmap.get(key);
		if (i != null) {
			return i;
		}

		try {
			i = Image.getJpegBestFit(path, desiredWidth, desiredHeight);
		} catch (Exception e) {
			// probably not a jpeg, we'll just ignore the exception and try again as a regular image.
			i = new Image(path);
		}
		int actualWidth = i.getWidth();
		int actualHeight = i.getHeight();

		if ((actualWidth != desiredWidth) || (actualHeight != desiredHeight) ) {
			i =
				i.smoothScaledFixedAspectRatio(
					desiredWidth < desiredHeight ? desiredWidth : desiredHeight,
					               desiredWidth > desiredHeight);
		}
		// disabled mimap for now
		// mipmap.put(key, i);
		return i;
	}

	public Image getImage(RequestOptions options, int desiredWidth, int desiredHeight) throws IOException, ImageException {
		final String key = desiredWidth + "x" + desiredHeight;
		Image i = mipmap.get(key);
		if (i != null) {
			return i;
		}

		if ((sif == null) || "image/jpeg".equals(sif.getMimeType())) {
			try {
				i = Image.getJpegBestFit(path, desiredWidth, desiredHeight);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				i = new Image(path);
			}
			int actualWidth = i.getWidth();
			int actualHeight = i.getHeight();

			System.out.println("AFTER SCALE " + actualWidth + ", " + actualHeight);
			System.out.println("DESIRED " + desiredWidth + ", " + desiredHeight);

			if ((actualWidth != desiredWidth) || (actualHeight != desiredHeight) ) {
				if ((options == null) || options.keepAspectRatio) {
					i = i.hwScaledFixedAspectRatio(desiredWidth < desiredHeight ? desiredWidth : desiredHeight,
					                                              desiredWidth > desiredHeight);
				} else {
					i = i.getHwScaledInstance(desiredWidth, desiredHeight);
				}
			}
			return i;
		} else {
			// not jpeg, just rescale
			i = new Image(path).getSmoothScaledInstance(desiredWidth, desiredHeight);
			//      mipmap.put(key, i);
			return i;
		}
	}

	public static RequestManager with(Container c) throws IOException {
		return new ImageLoader(null).new RequestManager(c);
	}

	public static class RequestOptions {

		boolean keepAspectRatio = true;

		public RequestOptions keepAspectRatio(boolean keepAspectRatio) {
			this.keepAspectRatio = keepAspectRatio;
			return this;
		}
	}

	public class RequestManager {

		Container c;
		String path;
		RequestOptions options;

		RequestManager(Container c) {
			this.c = c;
		}

		public RequestManager options(RequestOptions options) {
			this.options = options;
			return this;
		}

		public RequestBuilder load(String path) {
			this.path = path;

			return new RequestBuilder(options, path);
		}

		public class RequestBuilder implements Cloneable {

			String path;
			RequestOptions options;
			int scaleType;
			double focusX = 0.5f;
			double focusY = 0.5f;

			RequestBuilder(RequestOptions options, String path) {
				this.options = options;
				this.path = path;
			}

			public RequestBuilder scale(int scaleType) {
				this.scaleType = scaleType;
				return this;
			}

			public RequestBuilder into(ImageControl ic) throws IOException, ImageException {
				RequestBuilder builder = this;
				try {
					builder = (RequestBuilder) this.clone();
				} catch (CloneNotSupportedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				ImageLoader.this.path = builder.path;
				if (options == null) {
					options = new RequestOptions();
					options.keepAspectRatio = !ic.strechImage;
				}

				//        		SizeChangeHandler handler = new SizeChangeHandler(){
				//
				//      @Override
				//      public void onSizeChange(SizeChangeEvent event) {
				//    	  event.consumed = true;
				//        // TODO Auto-generated method stub
				//        final int width = event.width;
				//        final int height = event.height;

				//      }
				//    });
				ImageControlTarget target = new ImageControlTarget(ic);
				SizeReadyCallback callback = new SizeReadyCallback() {

					@Override
					public void onSizeReady(int width, int height) {
						Image i;
						try {
							switch (scaleType) {
								case ScaleType.CENTER: {
									i = new Image(RequestBuilder.this.path);
									//                int lastX = (width - i.getWidth()) / 2;
									//                int lastY = (height - i.getHeight()) / 2;

									ic.lastX = (width - i.getWidth()) / 2;
									ic.lastY = (height - i.getHeight()) / 2;

									//                target.ic.lastX= (int) ((width - i.getWidth()) * 0.4);
									//                target.ic.lastY = (int) ((height - i.getHeight()) * 0.3);
									ic.setImage(i, false);
									break;
								}
								case ScaleType.CENTER_CLIP: {
									i = new Image(RequestBuilder.this.path);
									int lastX = (width - i.getWidth()) / 2;
									int lastY = (height - i.getHeight()) / 2;

									i = i.getClippedInstance(-lastX, -lastY, width, height);
									ic.setImage(i);
									break;
								}
								case ScaleType.CENTER_CROP: {
									try {
										i = Image.getJpegBestFit(path, width, height);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										i = new Image(path);
									}
									if ((i.getWidth() != width) || (i.getHeight() != height) ) {
										double p1 = (double) width / i.getWidth();
										double p2 = (double) height / i.getHeight();
										i = i.hwScaledFixedAspectRatio(p1 < p2 ? height : width, p1 < p2);
									}
									//                target.ic.centerImage = true;
									ic.lastX = (width - i.getWidth()) / 2;
									ic.lastY = (height - i.getHeight()) / 2;
									ic.setImage(i, false);
									break;
								}
								case ScaleType.CENTER_INSIDE: {
									try {
										i = Image.getJpegBestFit(path, width, height);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										i = new Image(path);
									}
									if ((i.getWidth() > width) || (i.getHeight() > height) ) {
										double p1 = (double) width / i.getWidth();
										double p2 = (double) height / i.getHeight();
										i = i.hwScaledFixedAspectRatio(p1 > p2 ? width : height, p1 > p2);
									}
									//                target.ic.centerImage = true;
									ic.lastX = (width - i.getWidth()) / 2;
									ic.lastY = (height - i.getHeight()) / 2;
									ic.setImage(i, false);
									break;
								}
								case ScaleType.FIT_CENTER: {
									//                target.ic.centerImage = true;
									i = ImageLoader.this.getImage(options, width, height);

									ic.lastX = (width - i.getWidth()) / 2;
									ic.lastY = (height - i.getHeight()) / 2;
									ic.setImage(i, false);
									break;
								}
								case ScaleType.FIT_START: {
									i = ImageLoader.this.getImage(options, width, height);
									//                target.ic.centerImage = false;
									ic.lastX = 0;
									ic.lastY = 0;
									ic.setImage(i, false);
									break;
								}
								case ScaleType.FIT_END: {
									i = ImageLoader.this.getImage(options, width, height);
									//                target.ic.centerImage = false;

									ic.lastX = (width - i.getWidth()) / 1;
									ic.lastY = (height - i.getHeight()) / 1;
									ic.setImage(i, false);
									break;
								}
								case ScaleType.FIT_XY: {
									try {
										i = Image.getJpegBestFit(path, width, height);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										i = new Image(path);
									}
									if ((i.getWidth() != width) || (i.getHeight() != height) ) {
										i = i.getHwScaledInstance(width, height);
									}
									//                target.ic.centerImage = true;
									ic.lastX = 0;
									ic.lastY = 0;
									ic.setImage(i, false);
									break;
								}
								case ScaleType.FOCUS_CROP: {
									try {
										i = Image.getJpegBestFit(path, width, height);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										i = new Image(path);
									}
									if ((i.getWidth() != width) || (i.getHeight() != height) ) {
										double p1 = (double) width / i.getWidth();
										double p2 = (double) height / i.getHeight();
										i = i.smoothScaledFixedAspectRatio(p1 < p2 ? height : width, p1 < p2);
									}
									//                target.ic.centerImage = true;
									ic.lastX = (int) ((width - i.getWidth()) * focusX);
									ic.lastY = (int) ((height - i.getHeight()) * focusY);
									ic.setImage(i, false);
									break;
								}
								case ScaleType.NONE: {
									i = new Image(path);
									//                target.ic.centerImage = false;

									ic.lastX = 0;
									ic.lastY = 0;
									ic.setImage(i, false);
									break;
								}
							}

							//              options.keepAspectRatio = !ic.strechImage;
							//              target.ic.setImage(ImageLoader.this.getImage(options, width, height));
						} catch (IOException | ImageException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};

				//        		ic.addSizeChangeHandler(handler);
				target.getSize(callback);
				if ((ic.getWidth() > 0) || (ic.getHeight() > 0)) {
					//        	handler.onSizeChange(new SizeChangeEvent(ic, ic.getWidth(), ic.getHeight()));
					callback.onSizeReady(ic.getWidth(), ic.getHeight());
				}
				return this;
			}

			public RequestBuilder focusPoint(double x, double y) {
				this.focusX = x;
				this.focusY = y;
				return this;
			}
		}
	}

	static interface SizeReadyCallback {
		void onSizeReady(int width, int height);
	}

	static interface Target<C extends Control> {
		void getSize(SizeReadyCallback callback);
	}
}
