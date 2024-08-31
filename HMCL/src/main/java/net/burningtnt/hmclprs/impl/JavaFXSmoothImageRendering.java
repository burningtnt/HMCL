package net.burningtnt.hmclprs.impl;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.ImageViewHelper;
import com.sun.javafx.sg.prism.NGImageView;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.prism.Graphics;
import com.sun.prism.Image;
import com.sun.prism.Texture;
import javafx.scene.Node;
import org.jackhuang.hmcl.util.logging.Logger;

import java.lang.reflect.Field;

public final class JavaFXSmoothImageRendering {
    private JavaFXSmoothImageRendering() {
    }

    public static void initialize() {
        try {
            Class.forName(ImageViewHelper.class.getName(), true, ImageViewHelper.class.getClassLoader());

            Field accessorField = ImageViewHelper.class.getDeclaredField("imageViewAccessor");
            accessorField.setAccessible(true);
            ImageViewHelper.ImageViewAccessor delegate = (ImageViewHelper.ImageViewAccessor) accessorField.get(null);

            Field imageField = NGImageView.class.getDeclaredField("image");
            imageField.setAccessible(true);

            accessorField.set(null, new ImageViewHelper.ImageViewAccessor() {
                @Override
                public NGNode doCreatePeer(Node node) {
                    return new NGImageView() {
                        private boolean isSmooth = false;

                        @Override
                        public void setSmooth(boolean s) {
                            isSmooth = true;
                        }

                        @Override
                        protected void renderContent(Graphics g) {
                            if (!isSmooth) {
                                return;
                            }

                            Texture tex = g.getResourceFactory().getCachedTexture(getImage(), com.sun.prism.Texture.WrapMode.CLAMP_TO_EDGE);
                            tex.setLinearFiltering(true);
                            tex.unlock();
                            super.renderContent(g);
                        }

                        private Image getImage() {
                            Image image;
                            try {
                                image = (Image) imageField.get(this);
                            } catch (IllegalAccessException e) {
                                throw new IllegalAccessError(e.getMessage());
                            }
                            return image;
                        }
                    };
                }

                @Override
                public void doUpdatePeer(Node node) {
                    delegate.doUpdatePeer(node);
                }

                @Override
                public BaseBounds doComputeGeomBounds(Node node, BaseBounds bounds, BaseTransform tx) {
                    return delegate.doComputeGeomBounds(node, bounds, tx);
                }

                @Override
                public boolean doComputeContains(Node node, double localX, double localY) {
                    return delegate.doComputeContains(node, localX, localY);
                }
            });
        } catch (Throwable t) {
            Logger.LOG.warning("Cannot initialize JavaFXSmoothImageRendering.", t);
        }
    }
}
