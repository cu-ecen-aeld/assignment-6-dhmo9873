# Recipe created by recipetool
# This is the basis of a recipe and may need further editing in order to be fully functional.
# (Feel free to remove these comments when editing.)

# WARNING: the following LICENSE and LIC_FILES_CHKSUM values are best guesses - it is
# your responsibility to verify that the values are complete and correct.
#
# The following license files were not able to be identified and are
# represented as "Unknown" below, you will need to check them yourself:
#   LICENSE
# License type for the misc-modules package
LICENSE = "MIT"
# Checksum for the license file to verify the source during the build
LIC_FILES_CHKSUM = "file://LICENSE;md5=f098732a73b5f6f3430472f5b094ffdb"

# Inherit the module class for kernel building and update-rc.d for SysVinit script management
inherit module update-rc.d

# Sources: The remote git repository and local files for the Makefile patch and init script
SRC_URI = "git://git@github.com/cu-ecen-aeld/assignment-7-dhmo9873.git;protocol=ssh;branch=master \
           file://0001-Restrict-Makefile-to-scull-and-misc-modules-only.patch \
           file://misc-modules-init \
           "

# Versioning based on the git commit hash
PV = "1.0+git${SRCPV}"
# Specific commit revision to use for the build
SRCREV = "3af60251bd1ed17e6e7e062558b8b66b3e447593"

# Source directory definition
S = "${WORKDIR}/git"

# Pass the staging kernel directory to the Makefile so it builds against the target's kernel headers
EXTRA_OEMAKE:append = " KERNELDIR=${STAGING_KERNEL_DIR}"

# Init script name to be installed in /etc/init.d
INITSCRIPT_NAME = "misc-modules-init"
# Startup parameters: run in default runlevels with priority 97
INITSCRIPT_PARAMS = "defaults 97"

# Forcefully remove automatic kernel module dependencies to prevent dnf rootfs errors
RDEPENDS:${PN}:remove = "kernel-module-faulty-${KERNEL_VERSION} kernel-module-hello-${KERNEL_VERSION}"
RDEPENDS:${PN}:remove = "kernel-module-faulty kernel-module-hello"

# Inform the package manager that this package provides the faulty and hello modules
RPROVIDES:${PN} += "kernel-module-faulty kernel-module-hello"
# ---------------

# Compile task: Executes the root Makefile which generates version headers and compiles subdirectories
do_compile() {
    oe_runmake -C ${S}/misc-modules
}

# Install task: Manually places the multiple .ko files and the init script into the package destination
do_install() {
    # Create the kernel modules directory and install faulty.ko and hello.ko
    install -d ${D}/lib/modules/${KERNEL_VERSION}/extra
    install -m 0644 ${S}/misc-modules/faulty.ko ${D}/lib/modules/${KERNEL_VERSION}/extra/
    install -m 0644 ${S}/misc-modules/hello.ko ${D}/lib/modules/${KERNEL_VERSION}/extra/

    # Create the init.d directory and install the startup script
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/misc-modules-init ${D}${sysconfdir}/init.d/misc-modules-init
}

# Map the installed files to the main package for inclusion in the final image
FILES:${PN} += "${sysconfdir}/init.d/misc-modules-init"
FILES:${PN} += "/lib/modules/${KERNEL_VERSION}/extra/faulty.ko"
FILES:${PN} += "/lib/modules/${KERNEL_VERSION}/extra/hello.ko"
