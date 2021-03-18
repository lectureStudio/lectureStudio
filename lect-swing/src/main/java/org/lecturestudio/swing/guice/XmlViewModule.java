/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.swing.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Module;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.spi.LinkedKeyBinding;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.lecturestudio.core.inject.Injector;
import org.lecturestudio.core.util.AggregateBundle;
import org.lecturestudio.swing.view.SwingView;

public class XmlViewModule extends AbstractModule {

	private final Map<Class<?>, Class<?>> viewMap;


	public XmlViewModule(Map<Class<?>, Class<?>> viewMap) {
		this.viewMap = viewMap;
	}

	@Override
	protected void configure() {
		Provider<AggregateBundle> resourceProvider = getProvider(AggregateBundle.class);
		Provider<Injector> injectorProvider = getProvider(Injector.class);

		bindListener(new ViewBindingMatcher(), new ViewProvisioner(
				injectorProvider, resourceProvider, viewMap));
	}

	public static XmlViewModule create(Module viewModule) {
		final Map<Class<?>, Class<?>> viewMap = new HashMap<>();

		for (Element element : Elements.getElements(viewModule)) {
			element.acceptVisitor(new DefaultElementVisitor<Void>() {

				@Override
				public <T> Void visit(Binding<T> binding) {
					Class<?> key = binding.getKey().getTypeLiteral().getRawType();

					binding.acceptTargetVisitor(new DefaultBindingTargetVisitor<T, Void>() {

						@Override
						public Void visit(LinkedKeyBinding<? extends T> binding) {
							Class<?> target = binding.getLinkedKey().getTypeLiteral().getRawType();

							if (target.isAnnotationPresent(SwingView.class)) {
								viewMap.put(key, target);
							}

							return null;
						}

					});

					return viewMap.containsKey(key) ? null : this.visitOther(binding);
				}

				@Override
				public Void visitOther(Element element) {
					return null;
				}
			});
		}

		return new XmlViewModule(viewMap);
	}
}
