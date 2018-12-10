// Copyright 2018 OmegaTrace Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License

package services;

/**
 * This interface demonstrates how to create a component that is injected
 * into a controller. The interface represents a counter that returns a
 * incremented number each time it is called.
 *
 * The {@link AppModule} class binds this interface to the
 * {@link AtomicCounter} implementation.
 */
public interface Counter {
    int nextCount();
}