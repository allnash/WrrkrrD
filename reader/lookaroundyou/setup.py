from setuptools import setup

setup(
    name='lookaroundyou',
    packages=['lookaroundyou'],
    version='0.0.1',
    description='A tshark wrapper to count the number of cellphones in the vicinity',
    author='allnash',
    author_email='gadre@omegatrace.com',
    download_url='',
    keywords=['tshark', 'wifi', 'location'],
    classifiers=[],
    install_requires=[
        "click",
        "netifaces",
        "pick",
        "requests",
        "certifi",
        "chardet",
        "idna",
        "urllib3"
    ],
    setup_requires=[],
    tests_require=[],
    entry_points={'console_scripts': [
        'lookaroundyou = lookaroundyou.__main__:main',
    ], },
)
