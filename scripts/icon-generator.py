#!/usr/bin/env python
# -*- coding: utf-8 -*-

import PIL
import os
from PIL import ImageFont
from PIL import Image
from PIL import ImageDraw

font_file = "Roboto-Light.ttf"

densities = [
    "hdpi",
    "mdpi",
    "xhdpi",
    "xxhdpi",
    "xxxhdpi"
]

output = "../app/src/main/res"
xml_output = "../app/src/main/res/drawable/ic_clock_24h.xml"
name_format = "ic_clock_24h_%02d_%02d"

base_size = {
    'width': 24,
    'height': 24
}

white = (255,255,255)
transparent_colour = (0, 0, 0, 0)

def get_multiplier(density):
    return {
        'xxxhdpi': 4.00,
        'xxhdpi': 3.00,
        'xhdpi': 2.00,
        'hdpi': 1.50,
        'mdpi': 1.00,
        'ldpi': 0.75
    }.get(density, 1.0)

def multiply_round(values, multiplier):
    result = {}
    for key, value in values.iteritems():
        result[key] = int(round(value * multiplier, 0))
    return result


def get_font_size(density):
    return {
        'xxxhdpi': 32,
        'xxhdpi': 24,
        'xhdpi': 18,
        'hdpi': 14,
        'mdpi': 10,
        'ldpi': 8
    }.get(density, 10)

def write_image(hour, minute, directory, icon_size, font):
    time_string = "%d:%02d" % (hour, minute)
    icon = Image.new('RGBA', (icon_size['width'], icon_size['height']), transparent_colour)
    drawable = ImageDraw.Draw(icon)
    text_width, text_height = drawable.textsize(time_string, font)
    position = ((icon_size['width'] - text_width) / 2, (icon_size['height'] - text_height) / 2)
    drawable.text(position, time_string, white, font)
    filename = os.path.join(directory, name_format % (hour, minute)) + ".png"
    icon.save(filename)


def write_xml_file(xml_path):
    with open(xml_path, "w+") as xml_file:
        xml_file.write('<level-list xmlns:android="http://schemas.android.com/apk/res/android">\n')
        index = 0
        for hour in range(0, 24):
            for minute in range(0, 60):
                name = name_format % (hour, minute)
                drawable_name = "@drawable/%s" % name
                line = '\t<item android:maxLevel="%d" android:drawable="%s" />\n' % (index, drawable_name)
                xml_file.write(line)
                index += 1
        xml_file.write('</level-list>\n')

def main():
    os.chdir(os.path.dirname(os.path.realpath(__file__)))
    if not (os.path.exists(output)):
        os.makedirs(output)
    if not (os.path.exists(os.path.dirname(xml_output))):
        os.makedirs(os.path.dirname(xml_output))

    for density in densities:
        directory = os.path.join(output, "drawable-" + density)
        if not (os.path.exists(directory)):
            os.makedirs(directory)

    for density in densities:
        directory = os.path.join(output, "drawable-" + density)
        multiplier = get_multiplier(density)
        icon_size = multiply_round(base_size, multiplier)

        font = ImageFont.truetype(font_file, get_font_size(density))
        print density
        for hour in range(0, 24):
            for minute in range(0, 60):
                write_image(hour, minute, directory, icon_size, font)

    write_xml_file(xml_output)

main()
