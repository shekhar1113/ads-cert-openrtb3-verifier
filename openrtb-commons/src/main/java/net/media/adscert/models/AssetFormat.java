/*
 * Copyright © 2019 - present. MEDIA.NET ADVERTISING FZ-LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.media.adscert.models;

import net.media.adscert.models.validator.CheckExactlyOneNotNull;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

/** Created by shiva.b on 14/12/18. */
@CheckExactlyOneNotNull(fieldNames = {"title", "img", "video", "data"})
public class AssetFormat {

  private static final Integer DEFAULT_REQUIRED = 0;

  @NotNull private Integer id;
  private Integer req = DEFAULT_REQUIRED;
  @Valid private TitleAssetFormat title;
  private ImageAssetFormat img;
  @Valid private VideoPlacement video;
  @Valid private DataAssetFormat data;
  private Map<String, Object> ext;

  public @NotNull Integer getId() {
    return this.id;
  }

  public void setId(@NotNull Integer id) {
    this.id = id;
  }

  public Integer getReq() {
    return this.req;
  }

  public void setReq(Integer req) {
    this.req = req;
  }

  public @Valid TitleAssetFormat getTitle() {
    return this.title;
  }

  public void setTitle(@Valid TitleAssetFormat title) {
    this.title = title;
  }

  public ImageAssetFormat getImg() {
    return this.img;
  }

  public void setImg(ImageAssetFormat img) {
    this.img = img;
  }

  public @Valid VideoPlacement getVideo() {
    return this.video;
  }

  public void setVideo(@Valid VideoPlacement video) {
    this.video = video;
  }

  public @Valid DataAssetFormat getData() {
    return this.data;
  }

  public void setData(@Valid DataAssetFormat data) {
    this.data = data;
  }

  public Map<String, Object> getExt() {
    return this.ext;
  }

  public void setExt(Map<String, Object> ext) {
    this.ext = ext;
  }
}
