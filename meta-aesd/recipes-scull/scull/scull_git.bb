# Recipe created by recipetool
# This is the basis of a recipe and may need further editing in order to be fully functional.
# (Feel free to remove these comments when editing.)

# WARNING: the following LICENSE and LIC_FILES_CHKSUM values are best guesses - it is
# your responsibility to verify that the values are complete and correct.
#
# The following license files were not able to be identified and are
# represented as "Unknown" below, you will need to check them yourself:
#   LICENSE
# License type for the scull module package
LICENSE = "MIT"
# Checksum for the license file to ensure validity during the build process
LIC_FILES_CHKSUM = "file://LICENSE;md5=f098732a73b5f6f3430472f5b094ffdb"

# Inherit module class for kernel module building and update-rc.d for init script management
inherit module update-rc.d

# Sources: The git repository for ldd3 and local files for the Makefile patch and init script
SRC_URI = "git://git@github.com/cu-ecen-aeld/assignment-7-dhmo9873.git;protocol=ssh;branch=master \
           file://0001-Restrict-Makefile-to-scull-and-misc-modules-only.patch \
           file://scull-init \
           "

# Package versioning using the git source revision
PV = "1.0+git${SRCPV}"
# Specific git commit hash to fetch
SRCREV = "3af60251bd1ed17e6e7e062558b8b66b3e447593"

# Source directory set to the cloned git repository
S = "${WORKDIR}/git"

# Pass the staging kernel directory to the Makefile to ensure we build against the correct headers
EXTRA_OEMAKE:append = " KERNELDIR=${STAGING_KERNEL_DIR}"

# Configuration for the SysVinit script: script name and startup priority
INITSCRIPT_NAME = "scull-init"
INITSCRIPT_PARAMS = "defaults 98"

# Stop the module class from adding its own dependencies to avoid 'nothing provides' errors
RDEPENDS:${PN}:remove = "kernel-module-scull-${KERNEL_VERSION}"
RDEPENDS:${PN}:remove = "kernel-module-scull"

# Explicitly state that this package provides the scull kernel module
RPROVIDES:${PN} += "kernel-module-scull"
# ---------------

# Compile task: Run the Makefile in the source root to build the module and generate version headers
do_compile() {
    oe_runmake -C ${S}/scull
}

# Install task: Manually move the compiled .ko and the init script into the image destination directory
do_install() {
    # Create directory and install the scull.ko module into the standard kernel extra modules path
    install -d ${D}/lib/modules/${KERNEL_VERSION}/extra
    install -m 0644 ${S}/scull/scull.ko ${D}/lib/modules/${KERNEL_VERSION}/extra/

    # Create directory and install the scull-init script into /etc/init.d/
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/scull-init ${D}${sysconfdir}/init.d/scull-init
}

# Add the init script and the kernel module to the main package files list
FILES:${PN} += "${sysconfdir}/init.d/scull-init"
FILES:${PN} += "/lib/modules/${KERNEL_VERSION}/extra/scull.ko"
